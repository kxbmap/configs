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

import configs.testutil.JavaEnum
import configs.testutil.instance.enum._
import configs.testutil.instance.io._
import configs.testutil.instance.net._
import configs.testutil.instance.string._
import configs.testutil.instance.symbol._
import configs.testutil.instance.util._
import java.io.File
import java.net.{InetAddress, URI}
import java.nio.file.Path
import java.util.{Locale, UUID}
import scala.reflect.{ClassTag, classTag}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal

object FromStringTest extends Scalaprops {

  val fromString = {
    def to[A: Gen : Equal : ClassTag](implicit A: FromString[A]) =
      forAll { (a: A) =>
        A.read(A.show(a)).exists(Equal[A].equal(_, a))
      }.toProperties(s"to ${classTag[A].runtimeClass.getSimpleName}")

    Properties.list(
      to[String],
      to[Symbol],
      to[Enum.Value],
      to[JavaEnum],
      to[UUID],
      to[Locale],
      to[Path],
      to[File],
      to[InetAddress],
      to[URI]
    )
  }

  object Enum extends Enumeration {

    val Foo, Bar, Baz = Value

    implicit lazy val valueGen: Gen[Value] = {
      val vs = values.toList
      Gen.elements(vs.head, vs.tail: _*)
    }

    implicit lazy val valueEqual: Equal[Value] =
      Equal.equalA[Value]
  }

}
