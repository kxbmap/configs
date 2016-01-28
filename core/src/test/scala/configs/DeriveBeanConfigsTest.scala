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
import scala.collection.convert.decorateAsScala._
import scalaprops.Property.forAll
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal
import scalaz.syntax.equal._

object DeriveBeanConfigsTest extends Scalaprops {

  def checkDerived[A: Gen : Configs : ToConfigValue : Equal] =
    forAll { a: A =>
      val actual = Configs[A].extract(a.toConfigValue)
      val result = actual.exists(_ === a)
      if (!result) {
        println()
        println(s"actual: $actual")
        println(s"expected value: $a")
      }
      result
    }


  val bean1 = {
    implicit val configs: Configs[Bean1] =
      Configs.bean[Bean1]

    implicit val gen: Gen[Bean1] =
      Gen[Int].map(new Bean1(_))

    implicit val equal: Equal[Bean1] =
      Equal.equalA[Bean1]

    implicit val tcv: ToConfigValue[Bean1] =
      ToConfigValue.fromMap(b => Map(
        "a1" -> b.a1.toConfigValue
      ))

    checkDerived[Bean1]
  }


  val bean22 = {
    implicit val configs: Configs[Bean22] =
      Configs.bean[Bean22]

    implicit val gen: Gen[Bean22] =
      Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
        case (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22) =>
          new Bean22(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22)
      }

    implicit val equal: Equal[Bean22] =
      Equal.equalA[Bean22]

    implicit val tcv: ToConfigValue[Bean22] =
      ToConfigValue.fromMap(
        _.values().asScala.zipWithIndex.map {
          case (n, i) => s"a${i + 1}" -> n.toConfigValue
        }.toMap)

    checkDerived[Bean22]
  }


  val bean484 = {
    implicit val configs: Configs[Bean484] =
      Configs.bean[Bean484]

    implicit val gen: Gen[Bean484] =
      Gen.sequenceNArray(484, Gen[Int]).map(Bean484.fromArray)

    implicit val equal: Equal[Bean484] =
      Equal.equalA[Bean484]

    implicit val tcv: ToConfigValue[Bean484] =
      ToConfigValue.fromMap(
        _.values().asScala.zipWithIndex.map {
          case (n, i) => s"a${i + 1}" -> n.toConfigValue
        }.toMap)

    checkDerived[Bean484]
  }

}
