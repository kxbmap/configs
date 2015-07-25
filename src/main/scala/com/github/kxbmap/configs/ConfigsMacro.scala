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

package com.github.kxbmap.configs

import com.typesafe.config.Config
import scala.reflect.macros.blackbox

class ConfigsMacro(val c: blackbox.Context) {

  import c.universe._

  lazy val configType = typeOf[Config]

  def configsType(arg: Type) = appliedType(typeOf[Configs[_]].typeConstructor, arg)

  def atPathType(arg: Type) = appliedType(typeOf[AtPath[_]].typeConstructor, arg)


  def materialize[T: WeakTypeTag]: Expr[Configs[T]] = {
    val tpe = weakTypeOf[T]
    val ctors = tpe.decls.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic => m
    }.toSeq.sortBy {
      m => (!m.isPrimaryConstructor, -m.paramLists.map(_.length).sum)
    }
    if (ctors.isEmpty) {
      c.abort(c.enclosingPosition, s"$tpe must have a public constructor")
    }
    val ts = ctors.flatMap(_.paramLists.flatMap(_.map(_.info))).distinct

    val instances: Map[Type, (TermName, Tree)] = ts.map { t =>
      val cn = TermName(c.freshName("c"))
      val ct = atPathType(t)
      val ci = c.inferImplicitValue(ct, silent = false)
      (t, (cn, q"val $cn: $ct = $ci"))
    }(collection.breakOut)

    val config = TermName("config")
    val cs = ctors.map { ctor =>
      val argLists = ctor.paramLists.map { params =>
        params.map { p =>
          val (cn, _) = instances(p.info)
          val key = p.name.decodedName.toString
          q"$cn.extract($config)($key)"
        }
      }
      q"""
      new ${configsType(tpe)} {
        def extract($config: $configType): $tpe = new $tpe(...$argLists)
      }
      """
    }

    c.Expr[Configs[T]](q"""
      {
        ..${instances.values.map(_._2)}
        ${cs.reduceLeft((l, r) => q"$l orElse $r")}
      }
      """)
  }

}
