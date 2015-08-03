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
      abort(s"No Configs[${nameOf[A]}] generated")
    }
    (values, build[A](state, cs))
  }

  def build[A: WeakTypeTag](state: State, ctors: Seq[Ctor]): Tree = {
    val modules = ctors.collect {
      case ModuleCtor(_, module) => module
    }
    val mc = if (modules.nonEmpty) Some(modulesConfigs(modules)) else None
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

    def applies(cmp: ModuleSymbol): Seq[MethodCtor] =
      cmp.info.decls.sorted
        .collect {
          case m: MethodSymbol if m.isPublic && m.returnType <:< top && nameOf(m) == "apply" =>
            MethodCtor(top, cmp, m)
        }
        .sortBy(!_.method.isSynthetic)

    def collect(tpe: Type, sym: ClassSymbol): Seq[Ctor] = {
      if (sym.isSealed) {
        sym.knownDirectSubclasses.toSeq.sortBy(nameOf(_)).flatMap { s =>
          val cs = s.asClass
          collect(cs.toType, cs)
        }
      } else if (sym.isModuleClass) {
        Seq(ModuleCtor(top, sym.module.asModule))
      } else if (sym.isCaseClass) {
        applies(sym.companion.asModule)
      } else {
        constructors(tpe)
      }
    }
    collect(top, top.typeSymbol.asClass)
  }


  sealed trait Ctor {
    def toConfigs(state: State): Tree

    def isModuleCtor: Boolean = this match {
      case _: ModuleCtor => true
      case _             => false
    }
  }

  case class CtorCtor(retType: Type, tpe: Type, ctor: MethodSymbol) extends Ctor {
    def toConfigs(state: State): Tree =
      fromMethod(state, ctor, argLists => q"new $tpe(...$argLists): $retType")
  }

  case class MethodCtor(retType: Type, module: ModuleSymbol, method: MethodSymbol) extends Ctor {
    def toConfigs(state: State): Tree =
      fromMethod(state, method, argLists => q"$module.$method(...$argLists): $retType")
  }

  case class ModuleCtor(retType: Type, module: ModuleSymbol) extends Ctor {
    def toConfigs(state: State): Tree = EmptyTree
  }

  def fromMethod(state: State, m: MethodSymbol, newInstance: Seq[Seq[Tree]] => Tree): Tree = {
    def getOrAppendState(key: Type)(op: => (TermName, Tree)): TermName =
      state._1.find(_._1 =:= key).fold {
        val (n, v) = op
        state._1 += key -> n
        state._2 += v
        n
      }(_._2)

    val config = TermName("config")
    val hyphens: Map[String, String] = m.paramLists.flatMap(_.map { p =>
      val n = nameOf(p)
      n -> toLowerHyphenCase(n)
    })(collection.breakOut)

    val argLists = m.paramLists.map(_.map { p =>
      val paramType = p.info
      val paramName = nameOf(p)
      val hyphen = hyphens(paramName)
      val cn = getOrAppendState(paramType) {
        val fn = freshName()
        fn -> q"lazy val $fn = $configsCompanion[$paramType]"
      }
      if (hyphens.contains(hyphen) || hyphens.valuesIterator.count(_ == hyphen) > 1) {
        q"$cn.get($config, $paramName)"
      } else {
        val on = getOrAppendState(optionType(paramType)) {
          val fn = freshName()
          fn -> q"lazy val $fn = $configsCompanion.optionConfigs[$paramType]($cn)"
        }
        q"$on.get($config, $paramName).getOrElse($cn.get($config, $hyphen))"
      }
    })
    q"""
    $configsCompanion.onPath { $config: $configType =>
      ${newInstance(argLists)}
    }
    """
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
