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

import configs.util._
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
    def to[A: Converter.FromString : Gen : ClassTag](string: A => String = (_: A).toString) =
      forAll { (a: A) =>
        Converter[String, A].convert(string(a)) == a
      }.toProperties(s"to ${classTag[A].runtimeClass.getSimpleName}")

    Properties.list(
      to[Symbol](_.name),
      to[JavaEnum](),
      to[UUID](),
      to[Locale](),
      to[Path](),
      to[File](),
      to[InetAddress](_.getHostAddress),
      to[URI]()
    )
  }

  implicit lazy val uuidGen: Gen[UUID] =
    Gen[Array[Byte]].map(UUID.nameUUIDFromBytes)

  implicit lazy val localeGen: Gen[Locale] = {
    val ls = Locale.getAvailableLocales
    Gen.elements(ls.head, ls.tail: _*)
  }

  implicit lazy val pathGen: Gen[Path] =
    Gen.nonEmptyList(Gen.nonEmptyString(Gen.alphaChar)).map(p => Paths.get(p.head, p.tail.toList: _*))

  implicit lazy val fileGen: Gen[File] =
    pathGen.map(_.toFile)

  implicit lazy val inetAddressGen: Gen[InetAddress] = {
    val p = Gen.choose(0, 255)
    Apply[Gen].apply4(p, p, p, p)((a, b, c, d) => s"$a.$b.$c.$d").map(InetAddress.getByName)
  }

  implicit lazy val uriGen: Gen[URI] = {
    val str = Gen.nonEmptyString(Gen.alphaChar)
    val opt = Gen.option(str)
    Apply[Gen].apply3(opt, str, opt) {
      (scheme, ssp, fragment) => new URI(scheme.orNull, ssp, fragment.orNull)
    }
  }

}
