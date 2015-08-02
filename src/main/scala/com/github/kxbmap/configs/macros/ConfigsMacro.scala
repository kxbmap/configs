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
import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

class ConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materialize[A: WeakTypeTag]: Tree = {
    val targetType = abortIfAbstract(weakTypeOf[A])
    val ctors = targetType.decls.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic && nonEmptyParams(m) && !hasParamType(m, targetType) => m
    }.toSeq.sortBy {
      m => (!m.isPrimaryConstructor, -m.paramLists.foldLeft(0)(_ + _.length))
    }
    if (ctors.isEmpty) {
      abort(s"$targetType must have a public constructor")
    }
    val terms = new mutable.ArrayBuffer[(Type, TermName)]()
    val values = new ArrayBuffer[Tree]()
    val config = TermName("config")
    val cs = ctors.map { ctor =>
      val hyphens: Map[String, String] = ctor.paramLists.flatMap(_.map { p =>
        val n = nameOf(p)
        n -> toLowerHyphenCase(n)
      })(collection.breakOut)
      val argLists = ctor.paramLists.map(_.map { p =>
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
        new $targetType(...$argLists)
      }
      """
    }
    q"""
    ..$values
    ${cs.reduceLeft((l, r) => q"$l.orElse($r)")}
    """
  }

  def getOrAppend[A](m: mutable.Buffer[(Type, A)], key: Type, op: => A): A =
    m.find(_._1 =:= key).fold {
      val v = op
      m += key -> v
      v
    }(_._2)

}
