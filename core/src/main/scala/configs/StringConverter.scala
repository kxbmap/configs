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

trait StringConverter[A] {
  self =>

  def from(string: String): Result[A]

  def to(value: A): String

  def xmap[B](f: A => B, g: B => A): StringConverter[B] =
    new StringConverter[B] {
      def from(string: String): Result[B] = self.from(string).map(f)
      def to(value: B): String = self.to(g(value))
    }
}

object StringConverter extends StringConverterInstances {

  def apply[A](implicit A: StringConverter[A]): StringConverter[A] = A

  def from[A](f: String => Result[A], t: A => String): StringConverter[A] =
    new StringConverter[A] {
      def from(string: String): Result[A] = Result.Try(f(string)).flatten
      def to(value: A): String = t(value)
    }

  def fromTry[A](f: String => A, t: A => String): StringConverter[A] =
    from(s => Result.successful(f(s)), t)

}


sealed abstract class StringConverterInstances {

  implicit lazy val stringStringConverter: StringConverter[String] =
    new StringConverter[String] {
      def from(string: String): Result[String] = Result.successful(string)
      def to(value: String): String = value
    }

  implicit lazy val symbolStringConverter: StringConverter[Symbol] =
    StringConverter.fromTry(Symbol.apply, _.name)

  implicit def enumValueStringConverter[A <: Enumeration]: StringConverter[A#Value] =
    macro macros.StringConverterMacro.enumValueStringConverter[A]

  implicit def javaEnumStringConverter[A <: jl.Enum[A]](implicit A: ClassTag[A]): StringConverter[A] = {
    val clazz = A.runtimeClass.asInstanceOf[Class[A]]
    val enums = clazz.getEnumConstants
    StringConverter.from(
      s => Result.fromOption(enums.find(_.name() == s)) {
        ConfigError(s"$s is not a valid value for ${clazz.getName} (valid values: ${enums.mkString(", ")})")
      },
      _.name())
  }

  implicit lazy val uuidStringConverter: StringConverter[UUID] =
    StringConverter.fromTry(UUID.fromString, _.toString)

  implicit lazy val localeStringConverter: StringConverter[Locale] =
    StringConverter.from(
      s => Result.fromOption(Locale.getAvailableLocales.find(_.toString == s)) {
        ConfigError(s"$s is not an available locale")
      },
      _.toString)

  implicit lazy val pathStringConverter: StringConverter[Path] =
    StringConverter.fromTry(Paths.get(_), _.toString)

  implicit lazy val fileStringConverter: StringConverter[File] =
    StringConverter.fromTry(new File(_), _.getPath)

  implicit lazy val inetAddressStringConverter: StringConverter[InetAddress] =
    StringConverter.fromTry(InetAddress.getByName, _.getHostAddress)

  implicit lazy val uriStringConverter: StringConverter[URI] =
    StringConverter.fromTry(new URI(_), _.toString)

}
