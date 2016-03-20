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

object FromString {

  def apply[A](implicit A: FromString[A]): FromString[A] = A


  def from[A](f: String => Result[A]): FromString[A] =
    a => Result.Try(f(a)).flatten

  def Try[A](f: String => A): FromString[A] =
    a => Result.Try(f(a))


  implicit val stringFromString: FromString[String] =
    Result.successful(_)

  implicit lazy val symbolFromString: FromString[Symbol] =
    Try(Symbol.apply)

  implicit def enumValueFromString[A <: Enumeration]: FromString[A#Value] =
    macro macros.FromStringMacro.enumValueFromString[A]

  implicit def javaEnumFromString[A <: jl.Enum[A]](implicit A: ClassTag[A]): FromString[A] = {
    val enums = A.runtimeClass.asInstanceOf[Class[A]].getEnumConstants
    from { s =>
      enums.find(_.name() == s).fold(
        Result.failure[A](ConfigError(
          s"$s is not a valid value of ${A.runtimeClass.getName} (valid values: ${enums.mkString(", ")})")))(
        Result.successful)
    }
  }

  implicit lazy val uuidFromString: FromString[UUID] =
    Try(UUID.fromString)

  implicit lazy val localeFromString: FromString[Locale] =
    from { s =>
      Locale.getAvailableLocales.find(_.toString == s).fold(
        Result.failure[Locale](ConfigError(s"$s is not an available locale")))(
        Result.successful)
    }

  implicit lazy val pathFromString: FromString[Path] =
    Try(Paths.get(_))

  implicit lazy val fileFromString: FromString[File] =
    Try(new File(_))

  implicit lazy val inetAddressFromString: FromString[InetAddress] =
    Try(InetAddress.getByName)

  implicit lazy val uriFromString: FromString[URI] =
    Try(new URI(_))

}
