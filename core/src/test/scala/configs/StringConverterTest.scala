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

object StringConverterTest extends Scalaprops {

  val roundtrip = {
    def roundtrip[A: Gen : ClassTag](implicit A: StringConverter[A]) =
      forAll { (a: A) =>
        A.fromString(A.toString(a)) == Result.successful(a)
      }.toProperties(classTag[A].runtimeClass.getName)

    Properties.list(
      roundtrip[String],
      roundtrip[Symbol],
      roundtrip[Enum.Value],
      roundtrip[JavaEnum],
      roundtrip[UUID],
      roundtrip[Locale],
      roundtrip[Path],
      roundtrip[File],
      roundtrip[InetAddress],
      roundtrip[URI]
    )
  }

  object Enum extends Enumeration {

    val Foo, Bar, Baz = Value

    implicit lazy val valueGen: Gen[Value] = {
      val vs = values.toList
      Gen.elements(vs.head, vs.tail: _*)
    }
  }

}
