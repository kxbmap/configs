/*
 * Copyright 2013-2016 Tsukasa Kitachi
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

package configs.macros

import scala.reflect.macros.blackbox

private[macros] abstract class MacroUtil {

  val c: blackbox.Context

  import c.universe._

  lazy val tConfig =
    tq"_root_.com.typesafe.config.Config"

  lazy val ConfigException =
    q"_root_.com.typesafe.config.ConfigException"

  lazy val Configs =
    q"_root_.configs.Configs"

  def tConfigs(arg: Type): Tree =
    tq"_root_.configs.Configs[$arg]"

  lazy val tString = typeOf[String]

  def tOption(arg: Type): Type =
    appliedType(typeOf[Option[_]].typeConstructor, arg)

  def tTupleN(args: Seq[Type]): Tree =
    tq"_root_.scala.${TypeName(s"Tuple${args.length}")}[..$args]"

  lazy val Result =
    q"_root_.configs.Result"

  def resultApplyN(args: Seq[Tree]): Tree =
    q"$Result.${TermName(s"apply${args.length}")}(..$args)"

  def resultTupleN(args: Seq[Tree]): Tree =
    q"$Result.${TermName(s"tuple${args.length}")}(..$args)"

  def nameOf(sym: Symbol): String =
    sym.name.decodedName.toString

  def nameOf(tpe: Type): String =
    nameOf(tpe.typeSymbol)

  def encodedNameOf(sym: Symbol): String =
    sym.name.encodedName.toString

  def fullNameOf(sym: Symbol): String =
    sym.fullName

  def fullNameOf(tpe: Type): String =
    fullNameOf(tpe.typeSymbol)

  def freshName(): TermName =
    TermName(c.freshName())

  def freshName(name: String): TermName =
    TermName(c.freshName(name))

  def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  def error(msg: String): Unit =
    c.error(c.enclosingPosition, msg)

  def warning(msg: String): Unit =
    c.warning(c.enclosingPosition, msg)

  def info(msg: String, force: Boolean = false): Unit =
    c.info(c.enclosingPosition, msg, force)

  def echo(msg: String): Unit =
    c.echo(c.enclosingPosition, msg)

}
