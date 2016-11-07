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

private[macros] abstract class MacroBase {

  val c: blackbox.Context

  import c.universe._

  val TypeKey = "type"


  def tqTuple(args: Seq[Type]): Tree = tq"(..$args)"

  def tupleAt(t: TermName, i: Int): Tree = q"$t.${TermName(s"_${i + 1}")}"

  def tOption(arg: Type): Type =
    appliedType(typeOf[Option[_]].typeConstructor, arg)

  def tMap(k: Type, v: Type): Type =
    appliedType(typeOf[Map[_, _]].typeConstructor, k, v)


  def decodedName(s: Symbol): String = s.name.decodedName.toString

  def decodedName(t: Type): String = decodedName(t.typeSymbol)

  def encodedName(s: Symbol): String = s.name.encodedName.toString

  def encodedName(t: Type): String = encodedName(t.typeSymbol)

  def freshName(): TermName = TermName(c.freshName())

  def freshName(name: String): TermName = TermName(c.freshName(name))


  def isEmpty(xss: List[List[Symbol]]): Boolean =
    xss.lengthCompare(1) <= 0 && xss.forall(_.isEmpty)


  abstract class DerivingContext {

    type Cache <: InstanceCache

    def cache: Cache

    def naming: Tree

    private[this] val n = freshName("n")

    def configKey(field: String): Tree = q"$n($field)"

    def valDefs: List[Tree] = {
      val nv = q"val $n = $naming"
      nv :: cache.valDefs
    }
  }

  def defineInstance[T <: Target](t: T)(f: T => Tree)(implicit ctx: DerivingContext): Tree = {
    ctx.cache.putEmpty(t.tpe)
    val inst = ctx.cache.replace(t.tpe, f(t))
    q"""
      new _root_.java.lang.Object {
        ..${ctx.valDefs}
      }.$inst
     """
  }


  sealed trait Target {
    def tpe: Type
  }

  sealed trait SealedMember {
    def tpe: Type
  }

  case class SealedClass(tpe: Type, subclasses: List[SealedMember]) extends Target

  case class CaseClass(tpe: Type, params: List[Param], accessors: List[Accessor]) extends Target with SealedMember

  case class CaseObject(tpe: Type, module: ModuleSymbol) extends SealedMember

  case class ValueClass(tpe: Type, param: Param, accessor: Accessor) extends Target

  case class JavaBeans(tpe: Type, provider: InstanceProvider, properties: List[Property]) extends Target


  sealed trait InstanceProvider

  case class Constructor(tpe: Type) extends InstanceProvider

  case class NewInstance(tpe: Type, tree: Tree) extends InstanceProvider


  case class Param(symbol: Symbol, default: Option[ModuleMethod]) {
    def tpe: Type = symbol.info
    def name: String = decodedName(symbol)
    def hasDefault: Boolean = default.isDefined
  }

  case class ModuleMethod(module: ModuleSymbol, method: MethodSymbol)

  case class Accessor(method: MethodSymbol) {
    def tpe: Type = method.returnType
    def name: String = decodedName(method)
  }

  case class Property(name: String, tpe: Type, getter: MethodSymbol, setter: MethodSymbol)

  object Property {

    object Getter {
      def unapply(m: MethodSymbol): Option[(String, MethodSymbol, Type)] =
        if (!m.isPublic || !isEmpty(m.paramLists)) None
        else {
          val (s, n) = decodedName(m).span(_.isLower)
          val t = m.returnType
          if (n.nonEmpty && (s == "get" || s == "is" && (t =:= typeOf[Boolean] || t =:= typeOf[java.lang.Boolean])))
            Some((n, m, t))
          else None
        }
    }

    object Setter {
      def unapply(m: MethodSymbol): Option[(String, MethodSymbol, Type)] =
        if (!m.isPublic) None
        else m.paramLists match {
          case (p :: Nil) :: Nil =>
            val (s, n) = decodedName(m).span(_.isLower)
            if (n.nonEmpty && s == "set") Some((n, m, p.info))
            else None
          case _ => None
        }
    }

  }


  class TypeMap[A] private(buf: mutable.Buffer[(Type, A)]) {
    def +=(kv: (Type, A)): TypeMap[A] = {
      val idx = buf.indexWhere(_._1 =:= kv._1)
      if (idx >= 0) buf(idx) = kv
      else buf += kv
      this
    }

    def ++=(kv: Seq[(Type, A)]): TypeMap[A] =
      kv.foldLeft(this)(_ += _)

    def get(key: Type): Option[A] =
      buf.find(_._1 =:= key).map(_._2)

    def getOrElseUpdate(key: Type, value: => A): A =
      get(key).getOrElse {
        val v = value
        buf += ((key, v))
        v
      }

    def toList: List[(Type, A)] = buf.toList
  }

  object TypeMap {
    def empty[A]: TypeMap[A] =
      new TypeMap[A](mutable.Buffer.empty)

    def apply[A](values: (Type, A)*): TypeMap[A] =
      empty[A] ++= values
  }


  abstract class InstanceCache {
    private[this] val cache = TypeMap.empty[(TermName, Tree)]

    protected def getOrElseUpdate(tpe: Type, tree: => Tree): TermName =
      cache.getOrElseUpdate(tpe, (freshName("t"), tree))._1

    def instanceType(t: Type): Type

    def instance(tpe: Type): Tree

    def optInstance(inst: TermName): Tree

    def get(tpe: Type): TermName = getOrElseUpdate(tpe, instance(tpe))

    def getOpt(tpe: Type): TermName = getOrElseUpdate(tOption(tpe), optInstance(get(tpe)))

    def putEmpty(tpe: Type): TermName = {
      val n = freshName("t")
      cache += ((tpe, (n, EmptyTree)))
      n
    }

    def replace(tpe: Type, instance: Tree): TermName =
      cache.get(tpe).fold(c.abort(c.enclosingPosition, s"no entry for $tpe")) {
        case (n, _) => cache += ((tpe, (n, instance))); n
      }

    def valDefs: List[Tree] =
      cache.toList.collect {
        case (t, (n, tree)) if tree.nonEmpty =>
          q"lazy val $n: ${instanceType(t)} = $tree"
      }
  }

}
