/*
 * Copyright 2013-2016 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configs.macros

import scala.collection.mutable
import scala.reflect.macros.blackbox

private[macros] abstract class NHelper {

  val c: blackbox.Context

  import c.universe._

  lazy val tConfig =
    tq"_root_.com.typesafe.config.Config"

  lazy val Configs =
    q"_root_.configs.Configs"

  def tConfigs(arg: Type): Tree =
    tq"_root_.configs.Configs[$arg]"

  val MaxApplyN = 22
  val MaxTupleN = 22

  def applyResultN(n: Int): Tree =
    q"_root_.configs.Result.${TermName(s"apply$n")}"

  def tupleResultN(n: Int): Tree =
    q"_root_.configs.Result.${TermName(s"tuple$n")}"

  def tTupleN(n: Int): Tree =
    tq"_root_.scala.${TypeName(s"Tuple$n")}"

  def nameOf(sym: Symbol): String =
    sym.name.decodedName.toString

  def freshName(): TermName =
    TermName(c.freshName())

  def freshName(name: String): TermName =
    TermName(c.freshName(name))

  def length(xss: Seq[Seq[_]]): Int =
    xss.foldLeft(0)(_ + _.length)

  def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

}

class NConfigsMacro(val c: blackbox.Context) extends NHelper {

  import c.universe._

  def materializeConfigs[A: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[A]
    tpe.typeSymbol match {
      case typeSym if typeSym.isClass =>
        val classSym = typeSym.asClass
        if (classSym.isCaseClass)
          materializeCaseClass(tpe, classSym.companion.asModule)
        else
          abort(s"$tpe is not case class")

      case _ =>
        abort(s"$tpe is not class")
    }
  }


  private case class Param(sym: Symbol, site: Type) {
    val name = nameOf(sym)
    val term = sym.asTerm
    val tpe = sym.infoIn(site)

    def isImplicit: Boolean = term.isImplicit

    def isParamWithDefault: Boolean = term.isParamWithDefault
  }

  private def materializeCaseClass(tpe: Type, moduleSym: ModuleSymbol): Tree = {
    val apply =
      moduleSym.infoIn(tpe).decls.sorted
        .collectFirst {
          case m: MethodSymbol
            if m.isPublic && m.isSynthetic && m.returnType =:= tpe && nameOf(m) == "apply" => m
        }
        .getOrElse(abort(s"$moduleSym has no apply method"))

    val config = TermName("config")
    val paramLists = apply.paramLists.map(_.map(Param(_, tpe)))
    val configs = mutable.Buffer[(Type, TermName, Tree)]()
    def getConfigs(tpe: Type): TermName =
      configs.find(_._1 =:= tpe).map(_._2).getOrElse {
        val c = freshName("c")
        configs += ((tpe, c, q"$Configs[$tpe]"))
        c
      }

    def singleArgClass: Tree = {
      val a = freshName("a")
      val param = paramLists.collectFirst { case p :: _ => p }.getOrElse(abort("bug or broken"))
      val result = q"${getConfigs(param.tpe)}.get($config, ${param.name})"
      val args = paramLists.map(_.map(_ => a))
      q"""$result.map(($a: ${param.tpe}) => $moduleSym.$apply(...$args))"""
    }

    def smallClass(n: Int): Tree = {
      val parts = paramLists.map(_.map { p =>
        val a = freshName("a")
        (q"${getConfigs(p.tpe)}.get($config, ${p.name})", q"$a: ${p.tpe}", a)
      }.unzip3)
      val results = parts.flatMap(_._1)
      val params = parts.flatMap(_._2)
      val args = parts.map(_._3)
      q"""${applyResultN(n)}(..$results)(..$params => $moduleSym.$apply(...$args))"""
    }

    def largeClass(n: Int): Tree = {
      val results = paramLists.flatMap(_.map { p =>
        (p.tpe, q"${getConfigs(p.tpe)}.get($config, ${p.name})")
      })
      val g = {
        val d = (n + MaxTupleN - 1) / MaxTupleN
        (n + d - 1) / d
      }
      val (rs, params, gArgs) =
        results.grouped(g).map { group =>
          val m = group.length
          val r = q"""${tupleResultN(m)}(..${group.map(_._2)})"""
          val t = freshName("t")
          val tt = tq"${tTupleN(m)}[..${group.map(_._1)}]"
          val as = (1 to m).map(a => q"$t.${TermName(s"_$a")}")
          (r, q"$t: $tt", as)
        }.toList.unzip3
      val args = {
        @annotation.tailrec
        def fit(xs: List[Tree], shape: List[List[_]], acc: List[List[Tree]]): List[List[Tree]] =
          shape match {
            case Nil => acc.reverse
            case s :: ss =>
              val (h, t) = xs.splitAt(s.length)
              fit(t, ss, h :: acc)
          }
        fit(gArgs.flatten, paramLists, Nil)
      }
      q"""${applyResultN(rs.length)}(..$rs)(..$params => $moduleSym.$apply(...$args))"""
    }

    val body = length(paramLists) match {
      case 0 => abort("0-arg method is unsupported")
      case 1 => singleArgClass
      case n if n <= MaxApplyN => smallClass(n)
      case n if n <= MaxTupleN * MaxApplyN => largeClass(n)
      case n => abort(s"$apply is too large")
    }

    val vals = configs.map {
      case (_, n, t) => q"val $n = $t"
    }
    val instance =
      q"""
        $Configs.from[$tpe] { $config: $tConfig =>
          ..$vals
          $body
        }
       """

    val self = TermName("self")
    q"""
      implicit lazy val $self: ${tConfigs(tpe)} = $instance
      $self
     """
  }

}
