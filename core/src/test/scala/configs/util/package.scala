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

import com.typesafe.config.{ConfigUtil, ConfigValue}
import java.{lang => jl, util => ju}
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Property}
import scalaz.std.list._
import scalaz.std.map._
import scalaz.{Equal, Order}


package object util {

  val q = ConfigUtil.quoteString _

  type :@[A, T] = A { type Tag = T }

  implicit class TagOps[F[_], A](private val g: F[A]) extends AnyVal {
    def tag[T]: F[A :@ T] = g.asInstanceOf[F[A :@ T]]
  }

  implicit class UntagOps[F[_], A, T](private val g: F[A :@ T]) extends AnyVal {
    def untag: F[A] = g.asInstanceOf[F[A]]
  }

  implicit class ToConfigValueOps[A](private val self: A) {
    def toConfigValue(implicit A: ToConfigValue[A]): ConfigValue = A.toConfigValue(self)
  }


  def check[A: Configs : Gen : Equal : ToConfigValue]: Property =
    forAll { value: A =>
      val actual = Configs[A].extractValue(value.toConfigValue)
      val result = actual.exists(Equal[A].equal(_, value))
      if (!result) {
        println()
        println(s"actual: $actual")
        println(s"expected value: $value")
      }
      result
    }

  def check[A: Configs : Gen : Equal : ToConfigValue](id: String): Properties[String] =
    check[A].toProperties(id)


  implicit class GenOps[A](private val g: Gen[A]) extends AnyVal {
    def as[B >: A]: Gen[B] = g.asInstanceOf[Gen[B]]
  }


  implicit def javaListGen[A: Gen]: Gen[ju.List[A]] =
    Gen.list[A].map(_.asJava)

  implicit lazy val stringGen: Gen[String] = {
    import jl.{Character => C}
    val g = {
      val cp = (C.MIN_CODE_POINT to C.MAX_CODE_POINT).filter(C.isDefined)
      Gen.elements(cp.head, cp.tail: _*)
    }
    Gen.sized { size =>
      Gen.sequenceNArray(size, g).map { cps =>
        @tailrec
        def toChars(i: Int, j: Int, arr: Array[Char]): Array[Char] =
          if (i < cps.length) {
            val cs = C.toChars(cps(i))
            System.arraycopy(cs, 0, arr, j, cs.length)
            toChars(i + 1, j + cs.length, arr)
          } else {
            require(j == arr.length)
            arr
          }
        val cc = cps.foldLeft(0)(_ + C.charCount(_))
        new String(toChars(0, 0, new Array(cc)))
      }
    }
  }

  implicit lazy val symbolGen: Gen[Symbol] =
    stringGen.map(Symbol.apply)

  implicit def javaIterableEqual[F[_], A: Equal](implicit ev: F[A] <:< jl.Iterable[A]): Equal[F[A]] =
    Equal.equalBy(ev(_).asScala.toList)

  implicit def javaMapEqual[A: Order, B: Equal]: Equal[ju.Map[A, B]] =
    Equal.equalBy(_.asScala.toMap)

}
