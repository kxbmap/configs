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

class BeanConfigsMacro(val c: blackbox.Context) extends MacroUtil with Util {

  import c.universe._

  def deriveBeanConfigsA[A: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[A]
    if (tpe.typeSymbol.isAbstract) abort(s"$tpe must be concrete class")
    val hasNoArgCtor = tpe.decls.exists {
      case m: MethodSymbol => m.isConstructor && m.isPublic && m.paramLists.length <= 1 && m.paramLists.forall(_.isEmpty)
      case _ => false
    }
    if (!hasNoArgCtor) {
      abort(s"$tpe must have public no-arg constructor")
    }
    derive(tpe, q"new $tpe()")
  }

  def deriveBeanConfigsI[A: WeakTypeTag](newInstance: Tree): Tree = {
    val tpe = weakTypeOf[A]
    val obj = freshName("o")
    val nonNull =
      q"""
        val $obj: $tpe = ${c.untypecheck(newInstance)}
        _root_.scala.Predef.require($obj != null, "newInstance requires non null value")
        $obj
       """
    derive(tpe, nonNull)
  }

  private def derive(target: Type, newInstance: Tree): Tree = {
    implicit val ctx = new DerivationContext(target, newInstance)
    ctx.derive(beanConfigs)
  }


  private class DerivationContext(val target: Type, newInstance: Tree) {

    private val config = freshName("c")

    def derive(instance: Tree): Tree =
      instance

    def newInstanceVal(name: TermName): Tree =
      q"val $name: $target = $newInstance"

    def makeBeanConfigs(body: => Tree): Tree = {
      val b = body
      val vals = state.map {
        case (_, n, t) => q"val $n = $t"
      }
      q"""
        $Configs.fromConfig[$target] { $config: $tConfig =>
          ..$vals
          $b
        }
       """
    }

    def makeResult(tpe: Type, name: String): Tree =
      q"${configs(tpe)}.get($config, $name)"


    private type State = mutable.Buffer[(Type, TermName, Tree)]
    private val State = mutable.Buffer
    private val state: State = State.empty

    private def configs(tpe: Type): TermName =
      state.find(_._1 =:= tpe).map(_._2).getOrElse {
        val c = freshName("c")
        state += ((tpe, c, q"$Configs[$tpe]"))
        c
      }
  }

  private case class Property(
      tpe: Type, getter: MethodSymbol, setter: MethodSymbol, name: String, hyphenName: Option[String]) {

    val optType = tOption(tpe)

    def setOpt(bean: TermName, opt: Tree): Tree =
      q"$opt.foreach($bean.$setter)"

    def param(p: TermName): Tree =
      q"$p: $optType"

    def result()(implicit ctx: DerivationContext): Tree = {
      val r1 = ctx.makeResult(optType, name)
      hyphenName.fold(r1) { h =>
        val r2 = ctx.makeResult(optType, h)
        q"$r1.flatMap(o => if (o.isEmpty) $r2 else $Result.successful(o))"
      }
    }
  }

  private def listProperties(tpe: Type): List[Property] = {
    val ps =
      tpe.members.collect {
        case m: MethodSymbol if m.isPublic => (nameOf(m), m, m.paramLists)
      }.collect {
        case (n, m, Nil | List(Nil)) if n.startsWith("get") => (n.drop(3), m, m.returnType, true)
        case (n, m, List(List(p))) if n.startsWith("set") => (n.drop(3), m, p.info, false)
      }.groupBy(_._1).collect {
        case (n0, List((_, m1, t1, isG), (_, m2, t2, _))) if t1 =:= t2 && n0.headOption.exists(_.isUpper) =>
          val (g, s) = if (isG) (m1, m2) else (m2, m1)
          val n = n0.head.toLower +: n0.tail
          val h = toLowerHyphenCase(n0)
          (n, h, (t1, g, s))
      }.toList
    val (ns, hs, _) = ps.unzip3
    ps.map {
      case (n, h, (t, g, s)) => Property(t, g, s, n, validateHyphenName(n, h, ns, hs))
    }
  }

  private def beanConfigs(implicit ctx: DerivationContext): Tree = {
    val properties = listProperties(ctx.target)
    if (properties.isEmpty)
      abort(s"${fullNameOf(ctx.target)} has no bean properties")

    val bean = freshName("b")

    def singleBean: Tree = {
      val a = freshName("a")
      val (result, param, set) = properties match {
        case p :: Nil => (p.result(), p.param(a), p.setOpt(bean, Ident(a)))
        case _ => abort("bug or broken")
      }
      q"""
        $result.map { $param =>
          ${block(Seq(set))}
        }
       """
    }

    def smallBean: Tree = {
      val (results, params, sets) =
        properties.map { p =>
          val a = freshName("a")
          (p.result(), p.param(a), p.setOpt(bean, Ident(a)))
        }.unzip3
      q"""
        ${resultApplyN(results)} { ..$params =>
          ${block(sets)}
        }
       """
    }

    def largeBean: Tree = {
      val (results, params, sets) =
        grouping(properties).map { ps =>
          val tpl = resultTupleN(ps.map(_.result()))
          val tplType = tTupleN(ps.map(_.optType))
          val a = freshName("a")
          val sets = ps.zipWithIndex.map {
            case (p, i) => p.setOpt(bean, q"$a.${TermName(s"_${i + 1}")}")
          }
          (tpl, q"$a: $tplType", sets)
        }.unzip3
      q"""
        ${resultApplyN(results)} { ..$params =>
          ${block(sets.flatten)}
        }
       """
    }

    def block(sets: Seq[Tree]): Tree =
      q"""
        ${ctx.newInstanceVal(bean)}
        ..$sets
        $bean
       """

    ctx.makeBeanConfigs {
      properties.length match {
        case 0 => abort("bug or broken")
        case 1 => singleBean
        case n if n <= MaxApplyN => smallBean
        case n if n <= MaxTupleN * MaxApplyN => largeBean
        case _ => abort("too large bean")
      }
    }
  }

}
