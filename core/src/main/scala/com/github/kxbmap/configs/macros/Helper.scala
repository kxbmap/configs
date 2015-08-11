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

  def configsType[T: WeakTypeTag]: Type = configsType(weakTypeOf[T])

  def configsType(arg: Type): Type = appliedType(typeOf[Configs[_]].typeConstructor, arg)

  def optionType(arg: Type): Type = appliedType(typeOf[Option[_]].typeConstructor, arg)

  def setType(arg: Type): Type = appliedType(typeOf[Set[_]].typeConstructor, arg)

  lazy val configsCompanion = symbolOf[Configs[_]].companion

  lazy val badPathType = typeOf[ConfigException.BadPath]

  lazy val badValueType = typeOf[ConfigException.BadValue]


  def freshName(): TermName = TermName(c.freshName())

  def freshName(name: String): TermName = TermName(c.freshName(name))

  def nameOf(sym: Symbol): String = sym.name.decodedName.toString

  def nameOf(tpe: Type): String = nameOf(tpe.typeSymbol)

  def nameOf[A: WeakTypeTag]: String = nameOf(weakTypeOf[A])

  def encodedNameOf(sym: Symbol): String = sym.name.encodedName.toString

  def fullNameOf(sym: Symbol): String = sym.fullName

  def fullNameOf(tpe: Type): String = fullNameOf(tpe.typeSymbol)

  def fullNameOf[A: WeakTypeTag]: String = fullNameOf(weakTypeOf[A])

  def methodRepr(m: MethodSymbol): String =
    s"${fullNameOf(m)}${m.paramLists.map(paramListRepr).mkString}"

  def paramListRepr(ps: List[Symbol]): String = {
    val imp = ps.exists(_.isImplicit)
    ps.map(paramRepr).mkString(if (imp) "(implicit " else "(", ", ", ")")
  }

  def paramRepr(s: Symbol): String = {
    val t = s.asTerm
    val d = if (t.isParamWithDefault) " = <default>" else ""
    s"${nameOf(s)}: ${nameOf(t.info)}$d"
  }

  def zipWithParamPos(paramLists: List[List[Symbol]]): List[List[(Symbol, Int)]] =
    paramLists.zip(paramLists.scanLeft(1)(_ + _.length)).map {
      case (ps, s) => ps.zip(Stream.from(s))
    }


  def abort(msg: String): Nothing = c.abort(c.enclosingPosition, msg)

  def abortIfAbstract(tpe: Type): Type =
    if (tpe.typeSymbol.isAbstract)
      abort(s"$tpe must be concrete class")
    else
      tpe

  def warning(msg: String): Unit = c.warning(c.enclosingPosition, msg)

  def info(msg: String, force: Boolean = true) = c.info(c.enclosingPosition, msg, force)

}
