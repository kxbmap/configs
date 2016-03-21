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

  def from(s: String): Result[A]

  def map[B](f: A => B): FromString[B] =
    from(_).map(f)

  def flatMap[B](f: A => FromString[B]): FromString[B] =
    a => from(a).flatMap(f(_).from(a))
}

object FromString extends FromStringInstances {

  def apply[A](implicit A: FromString[A]): FromString[A] = A


  def from[A](f: String => Result[A]): FromString[A] =
    a => Result.Try(f(a)).flatten

  def fromTry[A](f: String => A): FromString[A] =
    a => Result.Try(f(a))

  def fromOption[A](f: String => Option[A], err: String => ConfigError): FromString[A] =
    from { s =>
      f(s).fold(Result.failure[A](err(s)))(Result.successful)
    }

}


sealed abstract class FromStringInstances {

  implicit val stringFromString: FromString[String] =
    Result.successful(_)

  implicit lazy val symbolFromString: FromString[Symbol] =
    FromString.fromTry(Symbol.apply)

  implicit def enumValueFromString[A <: Enumeration]: FromString[A#Value] =
    macro macros.FromStringMacro.enumValueFromString[A]

  implicit def javaEnumFromString[A <: jl.Enum[A]](implicit A: ClassTag[A]): FromString[A] = {
    val clazz = A.runtimeClass.asInstanceOf[Class[A]]
    val enums = clazz.getEnumConstants
    FromString.fromOption(
      s => enums.find(_.name() == s),
      s => ConfigError(s"$s is not a valid value for ${clazz.getName} (valid values: ${enums.mkString(", ")})"))
  }

  implicit lazy val uuidFromString: FromString[UUID] =
    FromString.fromTry(UUID.fromString)

  implicit lazy val localeFromString: FromString[Locale] =
    FromString.fromOption(
      s => Locale.getAvailableLocales.find(_.toString == s),
      s => ConfigError(s"$s is not an available locale"))

  implicit lazy val pathFromString: FromString[Path] =
    FromString.fromTry(Paths.get(_))

  implicit lazy val fileFromString: FromString[File] =
    FromString.fromTry(new File(_))

  implicit lazy val inetAddressFromString: FromString[InetAddress] =
    FromString.fromTry(InetAddress.getByName)

  implicit lazy val uriFromString: FromString[URI] =
    FromString.fromTry(new URI(_))

}
