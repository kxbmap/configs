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

import com.github.kxbmap.configs.util._
import java.io.File
import java.net.{InetAddress, URI}
import java.nio.file.{Path, Paths}
import java.util.{Locale, UUID}
import scala.reflect.{ClassTag, classTag}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Apply
import scalaz.std.string._

object ConverterTest extends Scalaprops {

  val identity = {
    def id[A: Gen : ClassTag](implicit A: Converter[A, A]) =
      forAll { a: A =>
        A.convert(a) == a
      }.toProperties(classTag[A].runtimeClass.getSimpleName)

    Properties.list(
      id[String],
      id[Int],
      id[Long]
    )
  }

  val fromString = {
    def to[A: Converter.FromString : ClassTag](expected: String => A)(implicit G: Gen[String :@ A]) =
      forAll { (s: String :@ A) =>
        Converter[String, A].convert(s) == expected(s)
      }.toProperties(s"to ${classTag[A].runtimeClass.getSimpleName}")

    Properties.list(
      to[Symbol](Symbol.apply),
      to[JavaEnum](JavaEnum.valueOf),
      to[UUID](UUID.fromString),
      to[Locale](s => Locale.getAvailableLocales.find(_.toString == s).get),
      to[Path](Paths.get(_)),
      to[File](new File(_)),
      to[InetAddress](InetAddress.getByName),
      to[URI](new URI(_))
    )
  }

  implicit lazy val symbolStringGen: Gen[String :@ Symbol] =
    Gen[String].tag[Symbol]

  implicit lazy val javaEnumStringGen: Gen[String :@ JavaEnum] =
    Gen[JavaEnum].map(_.name()).tag[JavaEnum]

  implicit lazy val uuidStringGen: Gen[String :@ UUID] =
    Gen[Array[Byte]].map(UUID.nameUUIDFromBytes).map(_.toString).tag[UUID]

  implicit lazy val localeStringGen: Gen[String :@ Locale] = {
    val ls = Locale.getAvailableLocales.map(_.toString)
    Gen.elements(ls.head, ls.tail: _*).tag[Locale]
  }

  implicit lazy val pathStringGen: Gen[String :@ Path] =
    Gen.nonEmptyList(Gen.nonEmptyString(Gen.alphaChar)).map(_.list.mkString(File.separator)).tag[Path]

  implicit lazy val fileStringGen: Gen[String :@ File] =
    pathStringGen.untag.tag[File]

  implicit lazy val inetAddressStringGen: Gen[String :@ InetAddress] = {
    val p = Gen.choose(0, 255)
    Apply[Gen].apply4(p, p, p, p)((a, b, c, d) => s"$a.$b.$c.$d").tag[InetAddress]
  }

  implicit lazy val uriStringGen: Gen[String :@ URI] = {
    val str = Gen.nonEmptyString(Gen.alphaChar)
    val opt = Gen.option(str)
    Apply[Gen].apply3(opt, str, opt) {
      (scheme, ssp, fragment) => s"${scheme.fold("")(_ + ":")}$ssp${fragment.fold("")("#" + _)}"
    }.tag[URI]
  }

}
