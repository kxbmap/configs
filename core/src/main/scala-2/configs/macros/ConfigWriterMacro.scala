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

import configs.{ConfigValue, ConfigWriter}
import scala.reflect.macros.blackbox

class ConfigWriterMacro(val c: blackbox.Context)
  extends MacroBase with Construct with ConfigWriterMacroImpl {

  import c.universe._

  def derive[A: WeakTypeTag](naming: Tree): Tree =
    deriveImpl(construct[A])(newContext(naming))

}

private[macros] trait ConfigWriterMacroImpl {
  this: MacroBase =>

  import c.universe._

  protected def deriveImpl(target: Target)(implicit ctx: DerivingWriterContext): Tree =
    defineInstance(target) {
      case SealedClass(t, ss) => sealedClass(t, ss)
      case CaseClass(t, _, as) => caseClass(t, as)
      case ValueClass(t, _, a) => valueClass(t, a)
      case JavaBeans(t, _, ps) => javaBeans(t, ps)
    }


  protected def newContext(naming: Tree): DerivingWriterContext =
    new DerivingWriterContext(naming, new ConfigWriterCache())

  class DerivingWriterContext(val naming: Tree, val cache: ConfigWriterCache) extends DerivingContext {
    type Cache = ConfigWriterCache

    // for writing we take the first name produced by ConfigKeyNaming
    def configKey(field: String): Tree = q"$n.applyFirst($field)"
  }

  class ConfigWriterCache extends InstanceCache {
    def instanceType(t: Type): Type = tConfigWriter(t)
    def instance(tpe: Type): Tree = q"$qConfigWriter[$tpe]"
    def optInstance(inst: TermName): Tree = q"$qConfigWriter.optionConfigWriter($inst)"
  }


  private val qConfigWriter = q"_root_.configs.ConfigWriter"

  private def tConfigWriter(arg: Type): Type =
    appliedType(typeOf[ConfigWriter[_]].typeConstructor, arg)

  private def sealedClass(tpe: Type, subs: List[SealedMember])(implicit ctx: DerivingWriterContext): Tree = {
    subs.map(_.tpe).foreach(ctx.cache.putEmpty)
    from(tpe) { o =>
      val str = ctx.cache.get(typeOf[String])
      val cases = subs.map {
        case CaseClass(t, _, as) =>
          val cc = ctx.cache.replace(t, fromKeyValues(t) { o =>
            val tkv = (q"$TypeKey", q"$str.write(${decodedName(t)})")
            (tkv :: Nil, caseAccessors(o, as))
          })
          cq"x: $t => $cc.write(x)"
        case CaseObject(t, _) =>
          val co = ctx.cache.replace(t, q"$str.contramap[$t](_.toString)")
          cq"x: $t => $co.write(x)"
      }
      q"$o match { case ..$cases }"
    }
  }

  private def caseClass(tpe: Type, accessors: List[Accessor])(implicit ctx: DerivingWriterContext): Tree =
    fromKeyValues(tpe)(o => (Nil, caseAccessors(o, accessors)))

  private def caseAccessors(o: TermName, accessors: List[Accessor])(implicit ctx: DerivingWriterContext): List[Append] =
    accessors.map { a =>
      val k = ctx.configKey(a.name)
      val v = q"$o.${a.method}"
      Append(ctx.cache.get(a.tpe), k, v)
    }

  private def valueClass(tpe: Type, accessor: Accessor)(implicit ctx: DerivingWriterContext): Tree =
    q"${ctx.cache.get(accessor.tpe)}.contramap(_.${accessor.method})"

  private def javaBeans(tpe: Type, props: List[Property])(implicit ctx: DerivingWriterContext): Tree =
    fromKeyValues(tpe) { o =>
      val (vals, refs) = props.partition(_.tpe <:< typeOf[AnyVal])
      val kvs = vals.map {
        case Property(n, t, g, _) =>
          val k = ctx.configKey(n)
          val v = q"${ctx.cache.get(t)}.write($o.$g())"
          (k, v)
      }
      val appends = refs.map {
        case Property(n, t, g, _) =>
          val k = ctx.configKey(n)
          val v = q"_root_.scala.Option($o.$g())"
          Append(ctx.cache.getOpt(t), k, v)
      }
      (kvs, appends)
    }


  private case class Append(writer: TermName, key: Tree, value: Tree)

  private def fromKeyValues(tpe: Type)(f: TermName => (List[(Tree, Tree)], List[Append])): Tree =
    from(tpe) { o =>
      val (kvs, appends) = f(o)
      val m0 = q"_root_.scala.collection.immutable.Map[${typeOf[String]}, ${typeOf[ConfigValue]}](..$kvs)"
      val m = appends.foldLeft(m0) {
        case (q, Append(w, k, v)) => q"$w.append($q, $k, $v)"
      }
      q"_root_.configs.ConfigObject.fromMap($m).value"
    }

  private def from(tpe: Type)(body: TermName => Tree): Tree = {
    val o = TermName("o")
    q"""
      $qConfigWriter.from { $o: $tpe =>
        ${body(o)}
      }
     """
  }

}
