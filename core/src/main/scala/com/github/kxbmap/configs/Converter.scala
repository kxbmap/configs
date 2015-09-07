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

import java.io.File
import java.net.{InetAddress, URI}
import java.nio.file.{Path, Paths}
import java.util.{Locale, UUID}
import java.{lang => jl}
import scala.reflect.{ClassTag, classTag}

trait Converter[A, B] {

  def convert(a: A): B

  def map[C](f: B => C): Converter[A, C] =
    convert(_) |> f

}

object Converter {

  def apply[A, B](implicit C: Converter[A, B]): Converter[A, B] = C


  type FromString[A] = Converter[String, A]

  object FromString {
    def apply[A](implicit A: FromString[A]): FromString[A] = A
  }


  private[this] final val _identity: Converter[Any, Any] = identity

  implicit def identityConverter[A]: Converter[A, A] =
    _identity.asInstanceOf[Converter[A, A]]


  implicit lazy val symbolFromString: FromString[Symbol] =
    Symbol.apply

  implicit def javaEnumFromString[A <: jl.Enum[A] : ClassTag]: FromString[A] = {
    val enums: Map[String, A] =
      classTag[A].runtimeClass.asInstanceOf[Class[A]].getEnumConstants.map(a => a.name() -> a)(collection.breakOut)
    s => enums.getOrElse(s, throw new NoSuchElementException(s"$s must be one of ${enums.keys.mkString(", ")}"))
  }

  implicit lazy val uuidFromString: FromString[UUID] =
    UUID.fromString

  implicit lazy val localeFromString: FromString[Locale] = {
    val availableLocales: Map[String, Locale] =
      Locale.getAvailableLocales.map(l => l.toString -> l)(collection.breakOut)
    s => availableLocales.getOrElse(s, throw new NoSuchElementException(s"Locale '$s' is not available"))
  }

  implicit lazy val pathFromString: FromString[Path] =
    Paths.get(_)

  implicit lazy val fileFromString: FromString[File] =
    new File(_)

  implicit lazy val inetAddressFromString: FromString[InetAddress] =
    InetAddress.getByName

  implicit lazy val uriFromString: FromString[URI] =
    new URI(_)

}
