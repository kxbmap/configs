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

  def configType = typeOf[Config]

  def configsType(arg: Type) = appliedType(typeOf[Configs[_]].typeConstructor, arg)

  def materialize[T: WeakTypeTag]: Expr[Configs[T]] = {
    val tpe = weakTypeOf[T]
    val ctor = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor && m.isPublic => m
    }.getOrElse {
      c.abort(c.enclosingPosition, s"$tpe must have a public primary constructor")
    }
    if (ctor.paramLists.lengthCompare(1) != 0) {
      c.abort(c.enclosingPosition, s"$ctor must have a single parameter list")
    }
    val params = ctor.paramLists.head

    val config = TermName("config")
    val args = params.map { p =>
      val key = p.name.decodedName.toString
      q"$config.get[${p.info}]($key)"
    }

    val result = q"""
      new ${configsType(tpe)} {
        def extract($config: $configType): $tpe = new $tpe(..$args)
      }
    """

    c.Expr[Configs[T]](result)
  }

}
