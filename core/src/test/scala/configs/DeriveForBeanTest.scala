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
import configs.testutil.instance.tuple._
import configs.testutil.{Bean1, Bean22, Bean484}
import java.util.Objects

import scala.beans.{BeanProperty, BooleanBeanProperty}
import scala.jdk.CollectionConverters._
import scalaprops.Property.forAll
import scalaprops.ScalapropsScalaz._
import scalaprops.{Gen, Lazy, Properties, Scalaprops}
import scalaz.Equal
import scalaz.syntax.apply._
import scalaz.syntax.equal._

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
        Lazy(Gen[(Int, Option[RecursiveBean])].map {
          case (n, b) => new RecursiveBean(n, b.orNull)
        })
      )
  }

  val recursive = check[RecursiveBean]


  val propertyName = {
    class Foo(
        @BeanProperty var FooBah: String,
        @BeanProperty var X: String,
        @BeanProperty var URL: String) {
      def this() = this(null, null, null)
    }

    implicit val gen: Gen[Foo] = {
      val s = Gen.nonEmptyString(Gen.alphaChar)
      (s |@| s |@| s)(new Foo(_, _, _))
    }

    implicit val naming: ConfigKeyNaming[Foo] = ConfigKeyNaming.identity

    forAll { foo: Foo =>
      val result = ConfigWriter[Foo].write(foo).asInstanceOf[ConfigObject]
      result.unwrapped.asScala.toSet == Set("URL" -> foo.getURL(), "fooBah" -> foo.getFooBah(), "x" -> foo.getX())
    }
  }


  val booleanProperties = {
    class BooleanProps(
        @BooleanBeanProperty var primitive: Boolean,
        @BooleanBeanProperty var wrapped: java.lang.Boolean) {
      def this() = this(false, false)
    }

    implicit val gen: Gen[BooleanProps] =
      (Gen[Boolean] |@| Gen[Boolean])(new BooleanProps(_, _))

    forAll { bool: BooleanProps =>
      val result = ConfigWriter[BooleanProps].write(bool).asInstanceOf[ConfigObject]
      result.unwrapped.asScala.toSet == Set("primitive" -> bool.isPrimitive(), "wrapped" -> bool.isWrapped())
    }
  }


  val preferIsGetter = {
    class Foo {
      def setBoolean(foo: Boolean): Unit = ()
      def isBoolean(): Boolean = true
      def getBoolean(): Boolean = false
    }

    forAll {
      val result = ConfigWriter[Foo].write(new Foo()).asInstanceOf[ConfigObject]
      result.unwrapped.asScala.toSet == Set("boolean" -> true.asInstanceOf[AnyRef])
    }
  }

}
