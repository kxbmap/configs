/*
 * Copyright 2013-2015 Tsukasa Kitachi
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

class ConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materialize[A: WeakTypeTag]: Tree = mat(weakTypeOf[A]).materialize()

  private def mat(tpe: Type): Mat = tpe.typeSymbol match {
    case ts if ts.isClass =>
      def nonEmpty[A](ctors: Seq[A]): Seq[A] =
        if (ctors.isEmpty) abort(s"$tpe has no instance creation methods")
        else ctors

      def classMat(ctors: Seq[MethodBase]): ClassMat = {
        val cs = nonEmpty(ctors)
        if (cs.exists(_.noArg)) {
          warning(s"$tpe has no-arg constructor")
        }
        ClassMat(tpe, cs)
      }

      val sym = ts.asClass
      if (sym.isSealed)
        SealedTypeMat(tpe, nonEmpty(collect(tpe, sym)))
      else if (sym.isCaseClass)
        classMat(applies(tpe, sym.companion.asModule))
      else if (!sym.isAbstract)
        classMat(constructors(tpe))
      else
        abort(s"$tpe should be concrete class or sealed trait")

    case _ =>
      abort(s"$tpe should be concrete class or sealed trait")
  }

  private def constructors(tpe: Type): Seq[CtorCtor] =
    tpe.decls.sorted.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic =>
        CtorCtor(tpe, m)
    }

  private def applies(tpe: Type, cmp: ModuleSymbol): Seq[MethodCtor] =
    cmp.info.decls.sorted
      .collect {
        case m: MethodSymbol if m.isPublic && m.returnType =:= tpe && nameOf(m) == "apply" =>
          MethodCtor(tpe, cmp, m)
      }
      .sortBy(!_.method.isSynthetic)

  private def collect(tpe: Type, sym: ClassSymbol): Seq[Ctor] = {
    if (sym.isSealed)
      sym.knownDirectSubclasses.toSeq.sortBy(_.fullName).flatMap { s =>
        val cs = s.asClass
        collect(cs.toType, cs)
      }
    else if (sym.isModuleClass)
      Seq(ModuleCtor(tpe, sym.module.asModule))
    else if (sym.isCaseClass)
      applies(tpe, sym.companion.asModule)
    else if (!sym.isAbstract)
      constructors(tpe)
    else
      Seq.empty
  }


  private sealed trait Mat {

    def tpe: Type

    def toConfigs(state: State): Tree

    def materialize(): Tree = {
      val self = TermName("self")
      val s = new State()
      val c = toConfigs(s)
      q"""
      ..${s.values}
      implicit lazy val $self: ${configsType(tpe)} = $c
      $self
      """
    }
  }

  private case class ClassMat(tpe: Type, ctors: Seq[MethodBase]) extends Mat {
    def toConfigs(state: State): Tree =
      ctors.map(_.toConfigs(state, tpe)).reduceLeft((l, r) => q"$l.orElse($r)")
  }

  private case class SealedTypeMat(tpe: Type, ctors: Seq[Ctor]) extends Mat {

    def toConfigs(state: State): Tree = {
      val mc = modulesConfigs
      val cs = ctors.map { ct =>
        ct.tpeName -> q"${state.addVal(ct.toConfigs(state, tpe))}"
      }
      val tc = byTypeConfigs(cs)
      val m = cs.toMap
      val ns = ctors.filterNot(_.isModuleCtor).flatMap(ct => m.get(ct.tpeName))

      (mc.toSeq ++: tc.toSeq ++: ns).reduceLeft((l, r) => q"$l.orElse($r)")
    }

    private def modulesConfigs: Option[Tree] = {
      val ms = ctors.collect {
        case ModuleCtor(_, module) =>
          val n = nameOf(module)
          (n, cq"$n => $module")
      }
      if (ms.isEmpty) None
      else {
        val (names, cases) = ms.unzip
        val tree =
          q"""
          new ${configsType(tpe)} {
            private[this] final val names = ${names.mkString(",")}
            def get(c: $configType, p: ${typeOf[String]}): $tpe =
              c.getString(p) match {
                case ..$cases
                case s => throw new $badValueType(c.origin(), p, s"unknown: $$s, expected is one of: $$names")
              }
          }
          """
        Some(tree)
      }
    }

    private def byTypeConfigs(cs: Seq[(String, Tree)]): Option[Tree] = {
      if (cs.isEmpty) None
      else {
        val cases = cs.map {
          case (n, t) => cq"$n => $t"
        }
        val tree =
          q"""
          $configsCompanion.onPath[${typeOf[String]}](_.getString("'type")).flatMap {
            case ..$cases
            case s => throw new ${typeOf[RuntimeException]}(s"unknown type: $$s")
          }
          """
        Some(tree)
      }
    }
  }


  private sealed trait Ctor {

    def tpe: Type

    def toConfigs(state: State, retType: Type): Tree

    def tpeName: String = nameOf(tpe)

    def isModuleCtor: Boolean = this match {
      case _: ModuleCtor => true
      case _             => false
    }
  }

  private case class CtorCtor(tpe: Type, method: MethodSymbol) extends Ctor with MethodBase {
    def newInstance(argLists: List[List[TermName]]): Tree = q"new $tpe(...$argLists)"
  }

  private case class MethodCtor(tpe: Type, module: ModuleSymbol, method: MethodSymbol) extends Ctor with MethodBase {
    def newInstance(argLists: List[List[TermName]]): Tree = q"$module.$method(...$argLists)"
  }

  private case class ModuleCtor(tpe: Type, module: ModuleSymbol) extends Ctor {
    def toConfigs(state: State, retType: Type): Tree = q"$configsCompanion.onPath[$retType](_ => $module)"
  }

  private sealed trait MethodBase {
    this: Ctor =>

    def newInstance(argLists: List[List[TermName]]): Tree

    def method: MethodSymbol

    def noArg: Boolean = method.paramLists.forall(_.isEmpty)

    lazy val companion = tpe.typeSymbol.companion

    lazy val defaultMethods: Map[Int, MethodSymbol] = companion match {
      case NoSymbol => Map.empty
      case cmp =>
        val prefix = s"${method.name.encodedName}$$default$$"
        cmp.info.decls.collect {
          case m: MethodSymbol if encodedNameOf(m).startsWith(prefix) =>
            val n = encodedNameOf(m)
            val i = n.lastIndexOf('$') + 1
            n.drop(i).toInt -> m
        }(collection.breakOut)
    }

    def toConfigs(state: State, retType: Type): Tree = {
      val config = TermName("config")
      val hyphens: Map[String, String] = method.paramLists.flatMap(_.map { p =>
        val n = nameOf(p)
        n -> toLowerHyphenCase(n)
      })(collection.breakOut)

      val vals = new mutable.ArrayBuffer[Tree]()
      val argLists = new mutable.ListBuffer[List[TermName]]()
      zipWithParamPos(method.paramLists).foreach { ps =>
        val args = ps.map {
          case (p, pos) =>
            val pt = p.asTerm
            val pType = p.infoIn(tpe)
            val optPType = optionType(pType)
            val pName = nameOf(p)
            val hName = hyphens(pName)
            val hNameEnabled = !hyphens.contains(hName) && hyphens.valuesIterator.count(_ == hName) <= 1

            val cn = state.getOrAppend(pType) {
              q"$configsCompanion[$pType]"
            }
            lazy val on = state.getOrAppend(optPType) {
              q"$configsCompanion[$optPType]"
            }

            val sn = if (pt.isImplicit || pt.isParamWithDefault) on else cn
            val fn = if (hNameEnabled) on else sn

            val first = q"$fn.get($config, $pName)"
            val second = if (hNameEnabled) Some(q"$sn.get($config, $hName)") else None
            val implicits =
              if (!pt.isImplicit) None
              else c.inferImplicitValue(pType) match {
                case EmptyTree if pt.isParamWithDefault => None
                case EmptyTree                          => abort(s"could not find implicit value for parameter ${paramRepr(p)}")
                case tree                               => Some(tree)
              }
            def default =
              if (pt.isParamWithDefault)
                defaultMethods.get(pos).map(m => q"$companion.$m(...$argLists)")
              else
                None

            val arg = freshName("a")
            val v = (second, implicits.orElse(default)) match {
              case (Some(s), Some(l)) => q"$first.orElse($s).getOrElse($l)"
              case (Some(s), _)       => q"$first.getOrElse($s)"
              case (_, Some(l))       => q"$first.getOrElse($l)"
              case _                  => first
            }
            vals += q"lazy val $arg = $v"
            arg
        }
        argLists += args
      }

      q"""
      $configsCompanion.onPath[$retType] { $config: $configType =>
        ..$vals
        ${newInstance(argLists.result())}
      }
      """
    }
  }


  private class State private(names: mutable.Buffer[(Type, TermName)], vals: mutable.Buffer[Tree]) {

    def this() = this(new mutable.ArrayBuffer[(Type, TermName)](), new mutable.ArrayBuffer[Tree]())

    def values: Seq[Tree] = vals

    def getOrAppend(key: Type)(op: => Tree): TermName =
      names.find(_._1 =:= key).fold {
        val n = addVal(op)
        names += key -> n
        n
      }(_._2)

    def addVal(tree: Tree): TermName = {
      val n = freshName("v")
      vals += q"lazy val $n = $tree"
      n
    }
  }

}
