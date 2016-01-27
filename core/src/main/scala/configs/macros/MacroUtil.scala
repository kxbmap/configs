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

  val MaxApplyN = 22
  val MaxTupleN = 22

  lazy val Result =
    q"_root_.configs.Result"

  def resultApplyN(n: Int): Tree =
    q"$Result.${TermName(s"apply$n")}"

  def resultTupleN(n: Int): Tree =
    q"$Result.${TermName(s"tuple$n")}"

  def tTupleN(n: Int): Tree =
    tq"_root_.scala.${TypeName(s"Tuple$n")}"

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

  def length(xss: Seq[Seq[_]]): Int =
    xss.foldLeft(0)(_ + _.length)

  def zipWithParamPos(paramLists: List[List[Symbol]]): List[List[(Symbol, Int)]] =
    paramLists.zip(paramLists.scanLeft(1)(_ + _.length)).map {
      case (ps, s) => ps.zip(Stream.from(s))
    }

  def fitShape[A](xs: List[A], shape: List[List[_]]): List[List[A]] = {
    if (xs.length != length(shape)) abort(s"mismatch length")
    @annotation.tailrec
    def loop(xs: List[A], shape: List[List[_]], acc: List[List[A]]): List[List[A]] =
      shape match {
        case Nil => acc.reverse
        case s :: ss =>
          val (h, t) = xs.splitAt(s.length)
          loop(t, ss, h :: acc)
      }
    loop(xs, shape, Nil)
  }

  def fitZip[A, B](xs: List[A], yss: List[List[B]]): List[List[(A, B)]] = {
    val xss = fitShape(xs, yss)
    xss.zip(yss).map(t => t._1.zip(t._2))
  }

  def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  def warning(msg: String): Unit =
    c.warning(c.enclosingPosition, msg)

  def echo(msg: String): Unit =
    c.echo(c.enclosingPosition, msg)

}
