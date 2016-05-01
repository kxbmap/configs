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

import configs.{ConfigValue, ToConfig}
import scala.reflect.macros.blackbox

class ToConfigMacro(val c: blackbox.Context)
  extends MacroBase with Construct with ToConfigMacroImpl {

  import c.universe._

  def derive[A: WeakTypeTag]: Tree =
    deriveImpl(construct[A])

}

private[macros] trait ToConfigMacroImpl {
  this: MacroBase =>

  import c.universe._

  protected def deriveImpl(target: Target): Tree = {
    implicit val cache = new ToConfigCache()
    defineInstance(target) {
      case SealedClass(t, ss) => sealedClass(t, ss)
      case CaseClass(t, _, as) => caseClass(t, as)
      case JavaBeans(t, _, ps) => javaBeans(t, ps)
    }
  }


  private val qToConfig = q"_root_.configs.ToConfig"

  private def tToConfig(arg: Type): Type =
    appliedType(typeOf[ToConfig[_]].typeConstructor, arg)

  private class ToConfigCache extends InstanceCache {
    def instanceType(t: Type): Type = tToConfig(t)
    def instance(tpe: Type): Tree = q"$qToConfig[$tpe]"
  }


  private def sealedClass(tpe: Type, subs: List[SealedMember])(implicit cache: ToConfigCache): Tree = {
    subs.map(_.tpe).foreach(cache.putEmpty)
    from(tpe) { o =>
      val str = cache.get(typeOf[String])
      val cases = subs.map {
        case CaseClass(t, _, as) =>
          val cc = cache.replace(t, fromKeyValues(t) { o =>
            val tkv = (TypeKey, q"$str.toValueOption(${decodedName(t)})")
            tkv :: caseAccessors(o, as)
          })
          cq"x: $t => $cc.toValue(x)"
        case CaseObject(t, m) =>
          val co = cache.replace(t, q"$str.contramap[$t](_ => ${decodedName(t)})")
          cq"x: $t => $co.toValue(x)"
      }
      q"$o match { case ..$cases }"
    }
  }

  private def caseClass(tpe: Type, accessors: List[Accessor])(implicit cache: ToConfigCache): Tree =
    fromKeyValues(tpe)(caseAccessors(_, accessors))

  private def caseAccessors(
      o: TermName, accessors: List[Accessor])(implicit cache: ToConfigCache): List[(String, Tree)] =
    accessors.map { a =>
      val k = toLowerHyphenCase(a.name)
      val v = q"${cache.get(a.tpe)}.toValueOption($o.${a.method})"
      (k, v)
    }

  private def javaBeans(tpe: Type, props: List[Property])(implicit cache: ToConfigCache): Tree =
    fromKeyValues(tpe)(o => props.map {
      case Property(n, t, g, _) =>
        val k = toLowerHyphenCase(n)
        val v =
          if (t <:< typeOf[AnyVal])
            q"${cache.get(t)}.toValueOption($o.$g)"
          else
            q"_root_.scala.Option($o.$g).flatMap(${cache.get(t)}.toValueOption)"
        (k, v)
    })

  private def fromKeyValues(tpe: Type)(kvs: TermName => List[(String, Tree)]): Tree =
    from(tpe) { o =>
      val t = tqTuple(Seq(typeOf[String], tOption(typeOf[ConfigValue])))
      val s = q"_root_.scala.Seq[$t](..${kvs(o)})"
      q"""
        $s.foldLeft(_root_.configs.ConfigObject.empty) {
          case (co, (k, v)) => v.foldLeft(co)(_.withValue(k, _))
        }
       """
    }

  private def from(tpe: Type)(body: TermName => Tree): Tree = {
    val o = TermName("o")
    q"""
      $qToConfig.from { $o: $tpe =>
        ${body(o)}
      }
     """
  }

}
