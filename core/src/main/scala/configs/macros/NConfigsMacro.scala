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

  lazy val ConfigException =
    q"_root_.com.typesafe.config.ConfigException"

  lazy val Configs =
    q"_root_.configs.Configs"

  def tConfigs(arg: Type): Tree =
    tq"_root_.configs.Configs[$arg]"

  lazy val tString = typeOf[String]

  val MaxApplyN = 22
  val MaxTupleN = 22

  lazy val Result =
    q"_root_.configs.Result"

  def applyResultN(n: Int): Tree =
    q"$Result.${TermName(s"apply$n")}"

  def tupleResultN(n: Int): Tree =
    q"$Result.${TermName(s"tuple$n")}"

  def tTupleN(n: Int): Tree =
    tq"_root_.scala.${TypeName(s"Tuple$n")}"

  def nameOf(sym: Symbol): String =
    sym.name.decodedName.toString

  def nameOf(tpe: Type): String =
    nameOf(tpe.typeSymbol)

  def fullNameOf(sym: Symbol): String =
    sym.fullName

  def fullNameOf(tpe: Type): String =
    fullNameOf(tpe.typeSymbol)

  def freshName(): TermName =
    TermName(c.freshName())

  def freshName(name: String): TermName =
    TermName(c.freshName(name))

  def length(xss: Seq[Seq[_]]): Int =
    xss.foldLeft(0)(_ + _.length)

  def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  def echo(msg: String): Unit =
    c.echo(c.enclosingPosition, msg)


  case class Param(sym: Symbol) {
    val name = nameOf(sym)
    val term = sym.asTerm
    val tpe = sym.info

    def isImplicit: Boolean = term.isImplicit

    def isParamWithDefault: Boolean = term.isParamWithDefault
  }

  sealed abstract class Method {
    def method: MethodSymbol

    def applyTrees(args: Seq[Seq[Tree]]): Tree

    def applyTerms(args: Seq[Seq[TermName]]): Tree =
      applyTrees(args.map(_.map(t => q"$t")))

    def paramLists: List[List[Param]] =
      method.paramLists.map(_.map(Param))
  }

  case class ApplyMethod(module: ModuleSymbol, method: MethodSymbol) extends Method {
    def applyTrees(args: Seq[Seq[Tree]]): Tree =
      q"$module.$method(...$args)"
  }

  case class Constructor(tpe: Type, method: MethodSymbol) extends Method {
    def applyTrees(args: Seq[Seq[Tree]]): Tree =
      q"new $tpe(...$args)"
  }

}

class NConfigsMacro(val c: blackbox.Context) extends NHelper {

  import c.universe._

  def materializeConfigs[A: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[A]
    val instance = tpe.typeSymbol match {
      case typeSym if typeSym.isClass =>
        val classSym = typeSym.asClass
        if (classSym.isAbstract) {
          if (classSym.isSealed)
            sealedClassConfigs(tpe, classSym)
          else
            abort(s"$tpe is abstract but not sealed")
        } else {
          if (classSym.isCaseClass)
            caseClassConfigs(tpe, classSym.companion.asModule)
          else
            plainClassConfigs(tpe)
        }

      case _ => abort(s"$tpe is not class")
    }
    val self = freshName("s")
    q"""
      implicit lazy val $self: ${tConfigs(tpe)} = $instance
      $self
     """
  }


  private def sealedClassConfigs(tpe: Type, classSym: ClassSymbol): Tree = {
    val knownSubclasses = {
      def go(cs: ClassSymbol): List[ClassSymbol] =
        if (cs.isSealed) {
          val subs = cs.knownDirectSubclasses.toList.map(_.asClass).flatMap(go)
          if (cs.isAbstract) subs else cs :: subs
        }
        else if (!cs.isAbstract) List(cs)
        else Nil
      go(classSym)
    }
    if (knownSubclasses.isEmpty)
      abort(s"$tpe has no known sub classes")

    val parts = knownSubclasses.map { cs =>
      val name = nameOf(cs)
      val inst =
        if (cs.isModuleClass)
          q"$Configs.fromConfig[$tpe](_ => $Result.successful(${cs.module}))"
        else if (cs.isCaseClass)
          caseClassConfigs(cs.toType, cs.companion.asModule, tpe)
        else
          plainClassConfigs(cs.toType, tpe)
      val module =
        if (cs.isModuleClass) Some(cs.module) else None
      (cq"$name => $inst", module.map(m => cq"$name => $m"))
    }

    val typeKey = "type"
    val typeInst =
      q"""
        $Configs.get[$tString]($typeKey).flatMap[$tpe] {
          case ..${parts.map(_._1)}
          case s => $Configs.failure("unknown type: " + s)
        }
       """
    if (parts.forall(_._2.isEmpty)) typeInst
    else {
      val moduleInst =
        q"""
          $Configs[$tString].map[$tpe] {
            case ..${parts.flatMap(_._2)}
            case s => throw new $ConfigException.Generic("unknown module: " + s)
          }
         """
      q"$moduleInst.orElse($typeInst)"
    }
  }

  private def caseClassConfigs(tpe: Type, module: ModuleSymbol): Tree =
    caseClassConfigs(tpe, module, tpe)

  private def caseClassConfigs(tpe: Type, module: ModuleSymbol, target: Type): Tree = {
    val applies = module.info.decls.sorted
      .collect {
        case m: MethodSymbol
          if m.isPublic && m.returnType =:= tpe && nameOf(m) == "apply" && length(m.paramLists) > 0 =>
          ApplyMethod(module, m)
      }
      // synthetic first
      .sortBy(!_.method.isSynthetic)

    if (applies.isEmpty)
      abort(s"$tpe has no available apply methods")
    else
      applies.map(methodConfigs(_, target)).reduceLeft((l, r) => q"$l.orElse($r)")
  }

  private def plainClassConfigs(tpe: Type): Tree =
    plainClassConfigs(tpe, tpe)

  private def plainClassConfigs(tpe: Type, target: Type): Tree = {
    val ctors = tpe.decls.sorted.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic && length(m.paramLists) > 0 =>
        Constructor(tpe, m)
    }
    if (ctors.isEmpty)
      abort(s"$tpe has no public constructor")
    else
      ctors.map(methodConfigs(_, target)).reduceLeft((l, r) => q"$l.orElse($r)")
  }

  private def methodConfigs(method: Method, target: Type): Tree = {
    val config = freshName("c")
    val paramLists = method.paramLists
    val configs = mutable.Buffer[(Type, TermName, Tree)]()
    def getConfigs(tpe: Type): TermName =
      configs.find(_._1 =:= tpe).map(_._2).getOrElse {
        val c = freshName("c")
        configs += ((tpe, c, q"$Configs[$tpe]"))
        c
      }

    def oneParamMethod: Tree = {
      val a = freshName("a")
      val param = paramLists.collectFirst { case p :: _ => p }.getOrElse(abort("bug or broken"))
      val result = q"${getConfigs(param.tpe)}.get($config, ${param.name})"
      val args = paramLists.map(_.map(_ => a))
      q"""$result.map(($a: ${param.tpe}) => ${method.applyTerms(args)})"""
    }

    def smallMethod(n: Int): Tree = {
      val parts = paramLists.map(_.map { p =>
        val a = freshName("a")
        (q"${getConfigs(p.tpe)}.get($config, ${p.name})", q"$a: ${p.tpe}", a)
      }.unzip3)
      val results = parts.flatMap(_._1)
      val params = parts.flatMap(_._2)
      val args = parts.map(_._3)
      q"""${applyResultN(n)}(..$results)(..$params => ${method.applyTerms(args)})"""
    }

    def largeMethod(n: Int): Tree = {
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
          val as = (1 to m).map(i => q"$t.${TermName(s"_$i")}")
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
      q"""${applyResultN(rs.length)}(..$rs)(..$params => ${method.applyTrees(args)})"""
    }

    val body = length(paramLists) match {
      case 0 => abort("0-arg method is unsupported")
      case 1 => oneParamMethod
      case n if n <= MaxApplyN => smallMethod(n)
      case n if n <= MaxTupleN * MaxApplyN => largeMethod(n)
      case _ => abort("too large param lists")
    }

    val vals = configs.map {
      case (_, n, t) => q"val $n = $t"
    }
    q"""
      $Configs.fromConfig[$target] { $config: $tConfig =>
        ..$vals
        $body
      }
     """
  }

}
