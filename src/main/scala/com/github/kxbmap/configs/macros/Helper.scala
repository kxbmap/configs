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
import com.typesafe.config.{Config, ConfigException}
import scala.reflect.macros.blackbox

private[macros] abstract class Helper {

  val c: blackbox.Context

  import c.universe._

  lazy val configType = typeOf[Config]

  def configsType(arg: Type) = appliedType(typeOf[Configs[_]].typeConstructor, arg)

  def optionType(arg: Type) = appliedType(typeOf[Option[_]].typeConstructor, arg)

  def setType(arg: Type) = appliedType(typeOf[Set[_]], arg)

  lazy val configsCompanion = symbolOf[Configs[_]].companion

  lazy val badPathType = typeOf[ConfigException.BadPath]


  def freshName(name: String): TermName = TermName(c.freshName(name))

  def nameOf(sym: Symbol): String = sym.name.decodedName.toString

  def nameOf(tpe: Type): String = nameOf(tpe.typeSymbol)

  def fullNameOf(sym: Symbol): String = sym.fullName

  def fullNameOf(tpe: Type): String = fullNameOf(tpe.typeSymbol)

  def nonEmptyParams(m: MethodSymbol): Boolean = m.paramLists.exists(_.nonEmpty)

  def hasParamType(m: MethodSymbol, tpe: Type): Boolean = m.paramLists.exists(_.exists(_.info =:= tpe))


  def abort(msg: String): Nothing = c.abort(c.enclosingPosition, msg)

  def abortIfAbstract(tpe: Type): Type =
    if (tpe.typeSymbol.isAbstract)
      abort(s"$tpe must be concrete class")
    else
      tpe

  def warning(msg: String): Unit = c.warning(c.enclosingPosition, msg)

}
