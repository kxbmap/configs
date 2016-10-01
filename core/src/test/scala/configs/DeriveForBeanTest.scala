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
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.string._
import configs.testutil.instance.tuple._
import configs.testutil.{Bean1, Bean22, Bean484}
import java.util.Objects
import scala.beans.BeanProperty
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.syntax.equal._
import scalaz.{Equal, Need}

object DeriveForBeanTest extends Scalaprops {

  val bean1 = {
    implicit val gen: Gen[Bean1] =
      Gen[Int].map(new Bean1(_))

    implicit val equal: Equal[Bean1] =
      Equal.equalA[Bean1]

    check[Bean1]
  }


  val bean22 = {
    implicit val gen: Gen[Bean22] =
      Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
        case (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22) =>
          new Bean22(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22)
      }

    implicit val equal: Equal[Bean22] =
      Equal.equalA[Bean22]

    check[Bean22]
  }


  val bean484 = {
    implicit val gen: Gen[Bean484] =
      Gen.sequenceNArray(484, Gen[Int]).map(Bean484.fromArray)

    implicit val equal: Equal[Bean484] =
      Equal.equalA[Bean484]

    check[Bean484]
  }


  class MyBean(
      @BeanProperty var a1: Int,
      @BeanProperty var a2: Int) {
    def this() = this(0, 0)
  }

  object MyBean {
    implicit val equal: Equal[MyBean] =
      Equal.equalBy(f => (f.a1, f.a2))
  }

  val withNewInstance = {
    val C = ConfigReader.deriveBeanWith(new MyBean(1, 42))
    val p1 =
      forAll { (a1: Int) =>
        val config = ConfigFactory.parseString(s"a-1 = $a1")
        C.extract(config).exists(_ === new MyBean(a1, 42))
      }
    val p2 =
      forAll { (a1: Int, a2: Int) =>
        val config = ConfigFactory.parseString(s"a-1 = $a1, a-2 = $a2")
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


  class RecursiveBean(
      @BeanProperty var value: Int,
      @BeanProperty var next: RecursiveBean) {
    def this() = this(0, null)

    override def equals(obj: Any): Boolean = obj match {
      case b: RecursiveBean => value == b.value && Objects.equals(next, b.next)
      case _ => false
    }
  }

  object RecursiveBean {
    implicit val equal: Equal[RecursiveBean] =
      Equal.equalA[RecursiveBean]

    implicit lazy val gen: Gen[RecursiveBean] =
      Gen.oneOfLazy(
        Need(Gen[(Int, Option[RecursiveBean])].map {
          case (n, b) => new RecursiveBean(n, b.orNull)
        })
      )
  }

  val recursive = check[RecursiveBean]

}
