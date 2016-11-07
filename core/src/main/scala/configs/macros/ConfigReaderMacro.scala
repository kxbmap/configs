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

import configs.ConfigReader
import scala.reflect.macros.blackbox

class ConfigReaderMacro(val c: blackbox.Context)
  extends MacroBase with Construct with ConfigReaderMacroImpl {

  import c.universe._

  def derive[A: WeakTypeTag](naming: Tree): Tree =
    deriveImpl(construct[A])(newContext(naming))

  def deriveBeanWith[A: WeakTypeTag](newInstance: Tree)(naming: Tree): Tree =
    deriveBeanWithImpl(constructBeans[A](newInstance))(newContext(naming))

}

private[macros] trait ConfigReaderMacroImpl {
  this: MacroBase =>

  import c.universe._

  protected def deriveImpl(target: Target)(implicit ctx: DerivingReaderContext): Tree =
    defineInstance(target) {
      case SealedClass(t, ss) => sealedClass(t, ss)
      case CaseClass(t, ps, _) => caseClass(t, ps)
      case ValueClass(t, p, _) => valueClass(t, p)
      case JavaBeans(t, p, ps) => javaBeans(t, p, ps)
    }

  protected def deriveBeanWithImpl(beans: JavaBeans)(implicit ctx: DerivingReaderContext): Tree =
    defineInstance(beans) {
      case JavaBeans(t, p, ps) => javaBeans(t, p, ps)
    }


  protected def newContext(naming: Tree): DerivingReaderContext =
    new DerivingReaderContext(naming, new ConfigReaderCache())

  class DerivingReaderContext(val naming: Tree, val cache: ConfigReaderCache) extends DerivingContext {
    type Cache = ConfigReaderCache
  }

  class ConfigReaderCache extends InstanceCache {
    def instanceType(t: Type): Type = tConfigReader(t)
    def instance(tpe: Type): Tree = q"$qConfigReader[$tpe]"
    def optInstance(inst: TermName): Tree = q"$qConfigReader.optionConfigReader($inst)"
  }


  private val qConfigReader = q"_root_.configs.ConfigReader"

  private def tConfigReader(arg: Type): Type =
    appliedType(typeOf[ConfigReader[_]].typeConstructor, arg)

  private val qResult = q"_root_.configs.Result"

  private def resultApply(args: Seq[Tree]): Tree =
    q"$qResult.${TermName(s"apply${args.length}")}(..$args)"

  private def resultTuple(args: Seq[Tree]): Tree =
    q"$qResult.${TermName(s"tuple${args.length}")}(..$args)"


  private def sealedClass(tpe: Type, subs: List[SealedMember])(implicit ctx: DerivingReaderContext): Tree = {
    subs.map(_.tpe).foreach(ctx.cache.putEmpty)
    val cos = subs.collect {
      case CaseObject(t, m) =>
        val c = ctx.cache.replace(t, q"$qConfigReader.successful($m)")
        cq"${decodedName(t)} => $c.as[$tpe]"
    }
    val obj = {
      val (ccs, ccq) = subs.collect {
        case CaseClass(t, ps, _) =>
          val c = ctx.cache.replace(t, caseClass(t, ps))
          val cc = q"$c.as[$tpe]"
          (cc, cq"${decodedName(t)} => $cc")
      }.unzip
      val cases = cos ++ ccq :+ cq"""s => $qConfigReader.failure("unknown type: " + s)"""
      ccs match {
        case Nil =>
          q"""
            $qConfigReader.get[${typeOf[String]}]($TypeKey).flatMap[$tpe] {
              case ..$cases
            }
           """
        case x :: xs =>
          val folded = xs.foldLeft(x)((l, r) => q"$l.orElse($r)")
          q"""
            $qConfigReader.get[${tOption(typeOf[String])}]($TypeKey).flatMap[$tpe] {
              _.fold($folded) {
                case ..$cases
              }
            }
           """
      }
    }
    q"""
      ${ctx.cache.get(typeOf[String])}.transform(_ => $obj, {
        case ..$cos
        case s => $qConfigReader.failure("unknown module: " + s)
      })
     """
  }

  private def caseClass(tpe: Type, params: List[Param])(implicit ctx: DerivingReaderContext): Tree = {
    val config = freshName("c")

    def read(p: Param): Tree = {
      val c = if (p.hasDefault) ctx.cache.getOpt(p.tpe) else ctx.cache.get(p.tpe)
      q"$c.read($config, ${ctx.configKey(p.name)})"
    }

    def aType(p: Param): Type =
      if (p.hasDefault) tOption(p.tpe) else p.tpe

    def value(p: Param, a: Tree): Tree =
      p.default.fold(a) {
        case ModuleMethod(mod, mth) => q"$a.getOrElse($mod.$mth)"
      }

    def noArg: Tree = q"$qResult.successful(new $tpe())"

    def single(p: Param): Tree = {
      val a = freshName("a")
      q"""
        ${read(p)}.map { ($a: ${aType(p)}) =>
          new $tpe(${value(p, Ident(a))})
        }
       """
    }

    def small: Tree = {
      val (gs, ps, vs) = params.map { p =>
        val a = freshName("a")
        (read(p), q"$a: ${aType(p)}", value(p, Ident(a)))
      }.unzip3
      q"""
        ${resultApply(gs)} { ..$ps =>
          new $tpe(..$vs)
        }
       """
    }

    def large: Tree = {
      val (gs, ps, vs) = grouping(params).map { ps =>
        val a = freshName("a")
        val at = tqTuple(ps.map(aType))
        val gs = resultTuple(ps.map(read))
        val vs = ps.zipWithIndex.map {
          case (p, i) => value(p, tupleAt(a, i))
        }
        (gs, q"$a: $at", vs)
      }.unzip3
      q"""
        ${resultApply(gs)} { ..$ps =>
          new $tpe(..${vs.flatten})
        }
       """
    }

    fromConfig(tpe, config) {
      params match {
        case Nil => noArg
        case p :: Nil => single(p)
        case ps if ps.lengthCompare(MaxApplySize) <= 0 => small
        case _ => large
      }
    }
  }

  private def valueClass(tpe: Type, param: Param)(implicit ctx: DerivingReaderContext): Tree =
    q"${ctx.cache.get(param.tpe)}.map(new $tpe(_))"

  private def javaBeans(
      tpe: Type, provider: InstanceProvider, props: List[Property])(implicit ctx: DerivingReaderContext): Tree = {
    val bean = freshName("b")
    val config = freshName("c")

    def block(setOps: List[Tree]): Tree =
      q"""
        val $bean = ${newInstance(provider)}
        ..$setOps
        $bean
       """

    def readOpt(p: Property): Tree =
      q"${ctx.cache.getOpt(p.tpe)}.read($config, ${ctx.configKey(p.name)})"

    def setOpt(p: Property, opt: Tree): Tree =
      q"$opt.foreach($bean.${p.setter})"

    def single(p: Property): Tree = {
      val a = freshName("a")
      val at = tOption(p.tpe)
      q"""
        ${readOpt(p)}.map { $a: $at =>
          ${block(setOpt(p, Ident(a)) :: Nil)}
        }
       """
    }

    def small: Tree = {
      val (gets, sets, params) = props.map { p =>
        val a = freshName("a")
        val at = tOption(p.tpe)
        (readOpt(p), setOpt(p, Ident(a)), q"$a: $at")
      }.unzip3
      q"""
        ${resultApply(gets)} { ..$params =>
          ${block(sets)}
        }
       """
    }

    def large: Tree = {
      val (gets, sets, params) = grouping(props).map { ps =>
        val a = freshName("a")
        val at = tqTuple(ps.map(p => tOption(p.tpe)))
        val gs = resultTuple(ps.map(readOpt))
        val ss = ps.zipWithIndex.map {
          case (p, i) => setOpt(p, tupleAt(a, i))
        }
        (gs, ss, q"$a: $at")
      }.unzip3
      q"""
        ${resultApply(gets)} { ..$params =>
          ${block(sets.flatten)}
        }
       """
    }

    fromConfig(tpe, config) {
      props match {
        case p :: Nil => single(p)
        case ps if ps.lengthCompare(MaxApplySize) <= 0 => small
        case _ => large
      }
    }
  }

  private def newInstance(provider: InstanceProvider): Tree =
    provider match {
      case Constructor(t) => q"new $t()"
      case NewInstance(_, n) =>
        q"""_root_.java.util.Objects.requireNonNull(${c.untypecheck(n)}, "newInstance must not be null")"""
    }

  private def fromConfig(tpe: Type, config: TermName)(body: => Tree): Tree =
    q"""
      $qConfigReader.fromConfig[$tpe] { $config: _root_.configs.Config =>
        $body
      }
     """

}
