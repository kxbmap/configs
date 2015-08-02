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
    val targetType = weakTypeOf[A]
    val ctors = constructors[A]
    if (ctors.isEmpty) {
      abort(s"$targetType must have a public constructor")
    }
    val self = TermName("self")
    val terms = new mutable.ArrayBuffer[(Type, TermName)]()
    val values = new mutable.ArrayBuffer[Tree]()
    val cs = ctors.map(ctorConfigs(_, terms, values))
    q"""
    ..$values
    implicit lazy val $self: ${configsType[A]} = ${cs.reduceLeft((l, r) => q"$l.orElse($r)")}
    $self
    """
  }


  sealed trait Ctor
  case class CtorCtor(tpe: Type, m: MethodSymbol) extends Ctor
  case class ApplyCtor(companion: ModuleSymbol, m: MethodSymbol) extends Ctor

  def constructors[A: WeakTypeTag]: Seq[Ctor] = {
    val tpe = weakTypeOf[A]
    val sym = symbolOf[A].asClass

    def ctorCtors: Seq[CtorCtor] =
      tpe.decls.sorted.collect {
        case m: MethodSymbol if m.isConstructor && m.isPublic && nonEmptyParams(m) && !hasParamType(m, tpe) =>
          CtorCtor(tpe, m)
      }

    def applyCtors: Seq[ApplyCtor] = {
      val companion = sym.companion.asModule
      val (synthetic, others) = companion.info.decls.sorted.collect {
        case m: MethodSymbol if m.isPublic && m.returnType =:= tpe && nameOf(m) == "apply" =>
          ApplyCtor(companion, m)
      }.partition(_.m.isSynthetic)
      synthetic ::: others
    }

    if (sym.isCaseClass) applyCtors else ctorCtors
  }

  def ctorConfigs(ctor: Ctor, terms: mutable.Buffer[(Type, TermName)], values: mutable.Buffer[Tree]): Tree = {
    def onPath(m: MethodSymbol, newInstance: Seq[Seq[Tree]] => Tree): Tree = {
      val config = TermName("config")
      val hyphens: Map[String, String] = m.paramLists.flatMap(_.map { p =>
        val n = nameOf(p)
        n -> toLowerHyphenCase(n)
      })(collection.breakOut)
      val argLists = m.paramLists.map(_.map { p =>
        val paramType = p.info
        val paramName = nameOf(p)
        val hyphen = hyphens(paramName)
        val cn = getOrAppend(terms, paramType, {
          val cn = freshName("c")
          values += q"lazy val $cn = $configsCompanion[$paramType]"
          cn
        })
        if (hyphens.contains(hyphen) || hyphens.valuesIterator.count(_ == hyphen) > 1) {
          q"$cn.get($config, $paramName)"
        } else {
          val on = getOrAppend(terms, optionType(paramType), {
            val on = freshName("c")
            values += q"lazy val $on = $configsCompanion.optionConfigs[$paramType]($cn)"
            on
          })
          q"$on.get($config, $paramName).getOrElse($cn.get($config, $hyphen))"
        }
      })
      q"""
      $configsCompanion.onPath { $config: $configType =>
        ${newInstance(argLists)}
      }
      """
    }
    ctor match {
      case CtorCtor(tpe, m)  => onPath(m, argLists => q"new $tpe(...$argLists)")
      case ApplyCtor(cmp, m) => onPath(m, argLists => q"$cmp(...$argLists)")
    }
  }


  def nonEmptyParams(m: MethodSymbol): Boolean = m.paramLists.exists(_.nonEmpty)

  def hasParamType(m: MethodSymbol, tpe: Type): Boolean = m.paramLists.exists(_.exists(_.info =:= tpe))

  def getOrAppend[A](m: mutable.Buffer[(Type, A)], key: Type, op: => A): A =
    m.find(_._1 =:= key).fold {
      val v = op
      m += key -> v
      v
    }(_._2)

}
