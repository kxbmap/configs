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

package configs

import java.io.File
import java.net.{InetAddress, URI}
import java.nio.file.{Path, Paths}
import java.util.{Locale, UUID}
import java.{lang => jl}
import scala.reflect.ClassTag

trait FromString[A] {
  self =>

  def read(string: String): Result[A]

  def show(value: A): String

  def xmap[B](f: A => B, g: B => A): FromString[B] =
    new FromString[B] {
      def read(string: String): Result[B] = self.read(string).map(f)
      def show(value: B): String = self.show(g(value))
    }
}

object FromString extends FromStringInstances {

  def apply[A](implicit A: FromString[A]): FromString[A] = A


  def from[A](f: String => Result[A], g: A => String): FromString[A] =
    new FromString[A] {
      def read(string: String): Result[A] = Result.Try(f(string)).flatten
      def show(value: A): String = g(value)
    }

  def fromTry[A](f: String => A, g: A => String): FromString[A] =
    from(s => Result.successful(f(s)), g)

  def fromOption[A](f: String => Option[A], err: String => ConfigError, g: A => String): FromString[A] =
    from(s => f(s).fold(Result.failure[A](err(s)))(Result.successful), g)

}


sealed abstract class FromStringInstances {

  implicit lazy val stringFromString: FromString[String] =
    new FromString[String] {
      def read(string: String): Result[String] = Result.successful(string)
      def show(value: String): String = value
    }

  implicit lazy val symbolFromString: FromString[Symbol] =
    FromString.fromTry(Symbol.apply, _.name)

  implicit def enumValueFromString[A <: Enumeration]: FromString[A#Value] =
    macro macros.FromStringMacro.enumValueFromString[A]

  implicit def javaEnumFromString[A <: jl.Enum[A]](implicit A: ClassTag[A]): FromString[A] = {
    val clazz = A.runtimeClass.asInstanceOf[Class[A]]
    val enums = clazz.getEnumConstants
    FromString.fromOption(
      s => enums.find(_.name() == s),
      s => ConfigError(s"$s is not a valid value for ${clazz.getName} (valid values: ${enums.mkString(", ")})"),
      _.name())
  }

  implicit lazy val uuidFromString: FromString[UUID] =
    FromString.fromTry(UUID.fromString, _.toString)

  implicit lazy val localeFromString: FromString[Locale] =
    FromString.fromOption(
      s => Locale.getAvailableLocales.find(_.toString == s),
      s => ConfigError(s"$s is not an available locale"),
      _.toString)

  implicit lazy val pathFromString: FromString[Path] =
    FromString.fromTry(Paths.get(_), _.toString)

  implicit lazy val fileFromString: FromString[File] =
    FromString.fromTry(new File(_), _.getPath)

  implicit lazy val inetAddressFromString: FromString[InetAddress] =
    FromString.fromTry(InetAddress.getByName, _.getHostAddress)

  implicit lazy val uriFromString: FromString[URI] =
    FromString.fromTry(new URI(_), _.toString)

}
