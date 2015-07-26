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

import com.github.kxbmap.configs.Configs
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

class ConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materialize[T: WeakTypeTag]: Expr[Configs[T]] = {
    val tpe = abortIfAbstract(weakTypeOf[T])
    val ctors = tpe.decls.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic && nonEmptyParams(m) && !hasParamType(m, tpe) => m
    }.toSeq.sortBy {
      m => (!m.isPrimaryConstructor, -m.paramLists.foldLeft(0)(_ + _.length))
    }
    if (ctors.isEmpty) {
      abort(s"$tpe must have a public constructor")
    }
    val m = new mutable.HashMap[Type, TermName]()
    val vs = new ArrayBuffer[Tree]()
    val config = TermName("config")
    val cs = ctors.map { ctor =>
      val hns: Map[String, String] = ctor.paramLists.flatMap(_.map { p =>
        val n = name(p)
        n -> toLowerHyphenCase(n)
      })(collection.breakOut)
      val argLists = ctor.paramLists.map(_.map { p =>
        val t = p.info
        val n = name(p)
        val hn = hns(n)
        val cn = m.getOrElseUpdate(t, {
          val cn = freshName("c")
          val ci = c.inferImplicitValue(atPathType(t), silent = false)
          vs += q"val $cn = $ci"
          cn
        })
        if (hns.contains(hn) || hns.valuesIterator.count(_ == hn) > 1) {
          q"$cn.extract($config)($n)"
        } else {
          val on = m.getOrElseUpdate(optionType(t), {
            val on = freshName("c")
            vs += q"val $on = $configsCompanion.optionAtPath[$t]($cn)"
            on
          })
          q"$on.extract($config)($n).getOrElse($cn.extract($config)($hn))"
        }
      })
      q"""
      new ${configsType(tpe)} {
        def extract($config: $configType): $tpe = new $tpe(...$argLists)
      }
      """
    }
    c.Expr[Configs[T]](
      q"""
      ..$vs
      ${cs.reduceLeft((l, r) => q"$l orElse $r")}
      """
    )
  }

}
