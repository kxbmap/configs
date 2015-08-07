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

package com.github.kxbmap.configs.macros

import scala.collection.mutable
import scala.reflect.macros.blackbox

class ConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materialize[A: WeakTypeTag]: Tree = {
    val self = TermName("self")
    val (values, cs) = build[A]
    q"""
    ..$values
    implicit lazy val $self: ${configsType[A]} = $cs
    $self
    """
  }

  type State = (mutable.ArrayBuffer[(Type, TermName)], mutable.ArrayBuffer[Tree])

  def build[A: WeakTypeTag]: (Seq[Tree], Tree) = {
    val terms = new mutable.ArrayBuffer[(Type, TermName)]()
    val values = new mutable.ArrayBuffer[Tree]()
    val state = (terms, values)
    val cs = ctors[A]
    if (cs.isEmpty) {
      abort(s"Couldn't materialize Configs[${fullNameOf[A]}]")
    }
    (values, build[A](state, cs))
  }

  def build[A: WeakTypeTag](state: State, ctors: Seq[Ctor]): Tree = {
    val modules = ctors.collect {
      case ModuleCtor(_, module) => module
    }
    val mc = if (modules.nonEmpty) Some(modulesConfigs[A](modules)) else None
    val others = ctors.filterNot(_.isModuleCtor).map(_.toConfigs(state))
    (mc.toSeq ++: others).reduceLeft((l, r) => q"$l.orElse($r)")
  }

  def ctors[A: WeakTypeTag]: Seq[Ctor] = {
    val top = weakTypeOf[A]

    def constructors(tpe: Type): Seq[CtorCtor] =
      tpe.decls.sorted.collect {
        case m: MethodSymbol if m.isConstructor && m.isPublic =>
          CtorCtor(top, tpe, m)
      }

    def applies(tpe: Type, cmp: ModuleSymbol): Seq[MethodCtor] =
      cmp.info.decls.sorted
        .collect {
          case m: MethodSymbol if m.isPublic && m.returnType <:< top && nameOf(m) == "apply" =>
            MethodCtor(top, tpe, cmp, m)
        }
        .sortBy(!_.method.isSynthetic)

    def collect(tpe: Type, sym: ClassSymbol): Seq[Ctor] = {
      if (sym.isSealed)
        sym.knownDirectSubclasses.toSeq.sortBy(nameOf(_)).flatMap { s =>
          val cs = s.asClass
          collect(cs.toType, cs)
        }
      else if (sym.isModuleClass)
        Seq(ModuleCtor(top, sym.module.asModule))
      else if (sym.isCaseClass)
        applies(tpe, sym.companion.asModule)
      else if (!sym.isAbstract)
        constructors(tpe)
      else
        Seq.empty
    }
    top.typeSymbol match {
      case ts if ts.isClass => collect(top, ts.asClass)
      case _                => Seq.empty
    }
  }


  sealed trait Ctor {

    def retType: Type

    def toConfigs(state: State): Tree

    def isModuleCtor: Boolean = this match {
      case _: ModuleCtor => true
      case _             => false
    }
  }

  case class CtorCtor(retType: Type, tpe: Type, method: MethodSymbol) extends Ctor with MethodBase {
    def newInstance(argLists: List[List[TermName]]): Tree = q"new $tpe(...$argLists)"
  }

  case class MethodCtor(retType: Type, tpe: Type, module: ModuleSymbol, method: MethodSymbol) extends Ctor with MethodBase {
    def newInstance(argLists: List[List[TermName]]): Tree = q"$module.$method(...$argLists)"
  }

  case class ModuleCtor(retType: Type, module: ModuleSymbol) extends Ctor {
    def toConfigs(state: State): Tree = EmptyTree
  }

  sealed trait MethodBase {
    this: Ctor =>

    def newInstance(argLists: List[List[TermName]]): Tree

    def method: MethodSymbol

    def tpe: Type

    lazy val companion = tpe.typeSymbol.companion

    lazy val defaultMethods: Map[Int, TermName] = companion match {
      case NoSymbol => Map.empty
      case cmp =>
        val mn = method.name.encodedName.toString
        val prefix = s"$mn$$default$$"
        cmp.info.decls.map(_.name.encodedName.toString).collect {
          case n if n.startsWith(prefix) =>
            val di = n.lastIndexOf('$') + 1
            n.drop(di).toInt -> TermName(n)
        }(collection.breakOut)
    }

    def toConfigs(state: State): Tree = {

      def stateGetOrAppend(key: Type)(op: => (TermName, Tree)): TermName =
        state._1.find(_._1 =:= key).fold {
          val (n, v) = op
          state._1 += key -> n
          state._2 += v
          n
        }(_._2)

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

            val cn = stateGetOrAppend(pType) {
              val fn = freshName("c")
              fn -> q"lazy val $fn = $configsCompanion[$pType]"
            }
            def on = stateGetOrAppend(optPType) {
              val fn = freshName("o")
              fn -> q"lazy val $fn = $configsCompanion.optionConfigs[$pType]($cn)"
            }

            val sn = if (pt.isParamWithDefault) on else cn
            val fn = if (hNameEnabled) on else sn

            val first = q"$fn.get($config, $pName)"
            val second = if (hNameEnabled) Some(q"$sn.get($config, $hName)") else None
            val default =
              if (pt.isParamWithDefault)
                defaultMethods.get(pos).map(n => q"$companion.$n(...$argLists)")
              else
                None

            val arg = freshName("a")
            val v = second ++ default match {
              case Seq(s, d) => q"$first.orElse($s).getOrElse($d)"
              case Seq(s)    => q"$first.getOrElse($s)"
              case Seq()     => first
              case _         => abort("library bug")
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

  def modulesConfigs[A: WeakTypeTag](modules: Seq[ModuleSymbol]): Tree = {
    val tpe = weakTypeOf[A]
    val (names, cqs) = modules.map {
      case m =>
        val n = nameOf(m)
        (n, cq"$n => $m")
    }.unzip
    q"""
    new ${configsType(tpe)} {
      private[this] final val names = ${names.mkString(",")}
      def get(c: $configType, p: ${typeOf[String]}): $tpe = {
        c.getString(p) match {
          case ..$cqs
          case s => throw new $badValueType(c.origin(), p, s"unknown: $$s, expected is one of: $$names")
        }
      }
    }
    """
  }

}
