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

import com.github.kxbmap.configs.{AtPath, Configs}
import com.typesafe.config.Config
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

class ConfigsMacro(val c: blackbox.Context) {

  import c.universe._

  lazy val configType = typeOf[Config]

  def configsType(arg: Type) = appliedType(typeOf[Configs[_]].typeConstructor, arg)

  def atPathType(arg: Type) = appliedType(typeOf[AtPath[_]].typeConstructor, arg)

  def optionType(arg: Type) = appliedType(typeOf[Option[_]].typeConstructor, arg)

  lazy val configsCompanion = symbolOf[Configs[_]].companion


  def materialize[T: WeakTypeTag]: Expr[Configs[T]] = {
    val tpe = weakTypeOf[T]
    if (tpe.typeSymbol.isAbstract) {
      c.abort(c.enclosingPosition, s"$tpe must be concrete class")
    }
    val ctors = tpe.decls.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic => m
    }.toSeq.sortBy {
      m => (!m.isPrimaryConstructor, -m.paramLists.map(_.length).sum)
    }
    if (ctors.isEmpty) {
      c.abort(c.enclosingPosition, s"$tpe must have a public constructor")
    }
    val m = new mutable.HashMap[Type, TermName]()
    val vs = new ArrayBuffer[Tree]()
    val config = TermName("config")
    val cs = ctors.map { ctor =>
      val argLists = ctor.paramLists.map { params =>
        params.map { p =>
          val t = p.info
          val k = p.name.decodedName.toString
          val h = toLowerHyphenCase(k)
          val cn = m.getOrElseUpdate(t, {
            val cn = TermName(c.freshName("c"))
            val ci = c.inferImplicitValue(atPathType(t), silent = false)
            vs += q"val $cn = $ci"
            cn
          })
          if (k == h) {
            q"$cn.extract($config)($k)"
          } else {
            val o = optionType(t)
            val on = m.getOrElseUpdate(o, {
              val on = TermName(c.freshName("c"))
              vs += q"val $on = $configsCompanion.optionAtPath[$t]($cn)"
              on
            })
            q"$on.extract($config)($k).getOrElse($cn.extract($config)($h))"
          }
        }
      }
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
