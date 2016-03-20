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

import com.typesafe.config.ConfigFactory
import configs.util._
import scala.beans.BeanProperty
import scala.collection.convert.decorateAsScala._
import scalaprops.Property.forAll
import scalaprops.{Properties, Gen, Scalaprops}
import scalaz.Equal
import scalaz.std.anyVal._
import scalaz.std.tuple._
import scalaz.std.string._
import scalaz.syntax.equal._

object DeriveBeanConfigsTest extends Scalaprops {

  val bean1 = {
    implicit val configs: Configs[Bean1] =
      Configs.deriveBean[Bean1]

    implicit val gen: Gen[Bean1] =
      Gen[Int].map(new Bean1(_))

    implicit val equal: Equal[Bean1] =
      Equal.equalA[Bean1]

    implicit val tcv: ToConfigValue[Bean1] =
      ToConfigValue.fromMap(b => Map(
        "a1" -> b.getA1.toConfigValue
      ))

    check[Bean1]
  }


  val bean22 = {
    implicit val configs: Configs[Bean22] =
      Configs.deriveBean[Bean22]

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

    check[Bean22]
  }


  val bean484 = {
    implicit val configs: Configs[Bean484] =
      Configs.deriveBean[Bean484]

    implicit val gen: Gen[Bean484] =
      Gen.sequenceNArray(484, Gen[Int]).map(Bean484.fromArray)

    implicit val equal: Equal[Bean484] =
      Equal.equalA[Bean484]

    implicit val tcv: ToConfigValue[Bean484] =
      ToConfigValue.fromMap(
        _.values().asScala.zipWithIndex.map {
          case (n, i) => s"a${i + 1}" -> n.toConfigValue
        }.toMap)

    check[Bean484]
  }


  class MyBean(
      @BeanProperty var a1: Int,
      @BeanProperty var a2: Int)

  object MyBean {
    implicit val equal: Equal[MyBean] =
      Equal.equalBy(f => (f.a1, f.a2))
  }

  val withNewInstance = {
    val C = Configs.deriveBeanWith(new MyBean(1, 42))
    val p1 =
      forAll { (a1: Int) =>
        val config = ConfigFactory.parseString(s"a1 = $a1")
        C.extract(config).exists(_ === new MyBean(a1, 42))
      }
    val p2 =
      forAll { (a1: Int, a2: Int) =>
        val config = ConfigFactory.parseString(s"a1 = $a1, a2 = $a2")
        (for {
          a <- C.extract(config)
          b <- C.extract(config)
        } yield a === b && (a ne b)).valueOrElse(false)
      }
    Properties.list(
      p1.toProperties("return new instance"),
      p2.toProperties("return different instances")
    )
  }

}
