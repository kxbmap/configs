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

trait Construct {
  this: MacroBase =>

  import c.universe._

  private def abort(a: Symbol, msg: String): Nothing =
    c.abort(c.enclosingPosition, s"cannot derive for `${a.fullName}`: $msg")

  protected def construct[A: WeakTypeTag]: Target = {
    def forClass(a: ClassSymbol): Target = {
      def err = abort(a, "not a concrete case class or Java Beans")
      if (a.typeParams.nonEmpty) abort(a, "polymorphic type")
      else if (a.isSealed) sealedClass(a)
      else if (a.isAbstract || a.isModuleClass) err
      else if (a.isDerivedValueClass) valueClass(a)
      else if (a.isCaseClass) caseClass(a)
      else javaBeans(a).getOrElse(err)
    }
    weakTypeOf[A].dealias.typeSymbol match {
      case a if a.isClass => forClass(a.asClass)
      case a => abort(a, "not a class")
    }
  }

  protected def constructBeans[A: WeakTypeTag](newInstance: Tree): JavaBeans = {
    val s = weakTypeOf[A].dealias.typeSymbol
    if (!s.isClass) abort(s, "not a class")
    else {
      val a = s.asClass
      if (a.typeParams.nonEmpty)
        abort(a, "polymorphic type")
      else if (a.isSealed || a.isAbstract || a.isModuleClass || a.isCaseClass)
        abort(a, "not Java Beans")
      else
        javaBeansWith(a, newInstance)
    }
  }


  private def sealedClass(a: ClassSymbol): Target = {
    def member(a: ClassSymbol): SealedMember =
      if (a.isModuleClass) CaseObject(a.toType, a.module.asModule)
      else caseClass(a)

    @annotation.tailrec
    def collect(css: List[ClassSymbol], acc: List[SealedMember]): List[SealedMember] = css match {
      case Nil => acc.reverse
      case s :: ss if s.isSealed =>
        val accN =
          if (s.isAbstract) acc
          else if (s.isCaseClass) member(s) :: acc
          else abort(a, s"$s is not abstract")
        s.typeSignature
        collect(s.knownDirectSubclasses.toList.map(_.asClass) ::: ss, accN)
      case s :: ss if !s.isAbstract && s.isCaseClass =>
        collect(ss, member(s) :: acc)
      case s :: _ =>
        abort(a, s"$s is not a concrete case class")
    }
    collect(a :: Nil, Nil) match {
      case Nil => abort(a, "no known subclasses")
      case (cc: CaseClass) :: Nil if cc.tpe =:= a.toType => cc
      case subclasses => SealedClass(a.toType, subclasses)
    }
  }

  private def defaults(a: ClassSymbol, m: MethodSymbol): Map[Int, ModuleMethod] =
    a.companion match {
      case cmp if cmp.isModule =>
        val mod = cmp.asModule
        val p = s"${m.name.encodedName}$$default$$"
        val pn = p.length
        mod.info.decls.collect {
          case d: MethodSymbol if d.isSynthetic => (encodedName(d), d)
        }.collect {
          case (n, d) if n.startsWith(p) => (n.drop(pn).toInt - 1, ModuleMethod(mod, d))
        }(collection.breakOut)
      case _ => Map.empty
    }

  private def caseClass(a: ClassSymbol): CaseClass = {
    val tpe = a.toType
    val params =
      tpe.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor =>
          if (m.paramLists.lengthCompare(1) <= 0) {
            val ds = defaults(a, m)
            m.paramLists.flatten.zipWithIndex.map {
              case (s, i) => Param(s, ds.get(i))
            }
          }
          else abort(a, "primary constructor has multi param list")
      }.getOrElse(abort(a, "bug?"))
    val accessors =
      tpe.decls.sorted.collect {
        case m: MethodSymbol if m.isCaseAccessor => Accessor(m)
      }
    CaseClass(tpe, params, accessors)
  }

  private def valueClass(a: ClassSymbol): ValueClass = {
    val tpe = a.toType
    val param =
      tpe.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor =>
          m.paramLists match {
            case (s :: Nil) :: Nil => Param(s, defaults(a, m).get(0))
            case _ => abort(a, "multi params value class")
          }
      }.getOrElse(abort(a, "no primary constructor"))
    val accessor =
      tpe.decls.collectFirst {
        case m: MethodSymbol if m.isParamAccessor => Accessor(m)
      }.getOrElse(abort(a, "no param accessor"))
    ValueClass(tpe, param, accessor)
  }

  private def javaBeans(a: ClassSymbol): Option[JavaBeans] = {
    val tpe = a.toType
    for {
      ctor <- tpe.decls.collectFirst {
        case m: MethodSymbol if m.isConstructor && m.isPublic && isEmpty(m.paramLists) =>
          Constructor(tpe)
      }
      props = listProperties(tpe) if props.nonEmpty
    } yield {
      if (props.lengthCompare(MaxSize) > 0)
        abort(a, s"more than $MaxSize properties")
      else
        JavaBeans(tpe, ctor, props)
    }
  }

  private def javaBeansWith(a: ClassSymbol, newInstance: Tree): JavaBeans = {
    val tpe = a.toType
    val props = listProperties(tpe)
    if (props.isEmpty)
      abort(a, "no properties")
    else if (props.lengthCompare(MaxSize) > 0)
      abort(a, s"more than $MaxSize properties")
    else
      JavaBeans(tpe, NewInstance(tpe, newInstance), props)
  }

  private def listProperties(tpe: Type): List[Property] = {
    val getters: Map[String, (MethodSymbol, Type)] =
      tpe.members.foldLeft(Map.empty[String, (MethodSymbol, Type)]) {
        case (m, Property.Getter((n, g, t))) =>
          if (m.contains(n) && decodedName(g).startsWith("get")) m
          else m + ((n, (g, t)))
        case (m, _) => m
      }
    tpe.members.sorted.collect {
      case Property.Setter(s@(n, _, _)) => (s, getters.get(n))
    }.collect {
      case ((n, s, t1), Some((g, t2))) if t1 =:= t2 => Property(n, t1, g, s)
    }
  }

}
