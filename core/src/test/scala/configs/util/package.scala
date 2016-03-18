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

import com.typesafe.config.{Config, ConfigList, ConfigMemorySize, ConfigObject, ConfigUtil, ConfigValue, ConfigValueFactory}
import java.{lang => jl, time => jt, util => ju}
import scala.annotation.tailrec
import scala.collection.convert.decorateAll._
import scala.collection.generic.CanBuildFrom
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Property}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.map._
import scalaz.std.string._
import scalaz.{Apply, Equal, Need, Order}


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

  implicit def javaStringMapGen[A: Gen]: Gen[ju.Map[String, A]] =
    Gen[Map[String, A]].map(_.asJava)

  implicit def javaSymbolMapGen[A: Gen]: Gen[ju.Map[Symbol, A]] =
    Gen[Map[String, A]].map(_.map(t => Symbol(t._1) -> t._2).asJava)

  implicit def javaSetGen[A: Gen]: Gen[ju.Set[A]] =
    Gen[Set[A]].map(_.asJava)

  implicit def javaIterableGen[A: Gen]: Gen[jl.Iterable[A]] =
    Gen[ju.List[A]].as[jl.Iterable[A]]

  implicit def javaCollectionGen[A: Gen]: Gen[ju.Collection[A]] =
    Gen[ju.List[A]].as[ju.Collection[A]]


  implicit def cbfMapGen[M[_, _], A: Gen, B: Gen](implicit g: Gen[Map[A, B]], cbf: CanBuildFrom[Nothing, (A, B), M[A, B]]): Gen[M[A, B]] =
    g.map(_.to[({type F[_] = M[A, B]})#F])


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

  implicit lazy val charGen: Gen[Char] = {
    import jl.{Character => C}
    Gen.choose(C.MIN_VALUE, C.MAX_VALUE).map(C.toChars(_)(0))
  }

  implicit lazy val javaCharacterGen: Gen[jl.Character] =
    charGen.map(Char.box)

  implicit lazy val floatGen: Gen[Float] =
    Gen.genFiniteFloat

  implicit lazy val javaFloatGen: Gen[jl.Float] =
    floatGen.map(Float.box)

  implicit lazy val doubleGen: Gen[Double] =
    Gen.genFiniteDouble

  implicit lazy val javaDoubleGen: Gen[jl.Double] =
    doubleGen.map(Double.box)

  implicit lazy val javaDurationGen: Gen[jt.Duration] =
    Gen.nonNegativeLong.map(jt.Duration.ofNanos)

  implicit lazy val configMemorySizeGen: Gen[ConfigMemorySize] =
    Gen.nonNegativeLong.map(ConfigMemorySize.ofBytes)


  implicit def genConfigValue0[A: Gen : ToConfigValue]: Gen[ConfigValue :@ A] =
    Gen[A].map(_.toConfigValue).tag[A]

  def genConfigValue[A: ToConfigValue](g: Gen[A]): Gen[ConfigValue] =
    genConfigValue0[A](g, ToConfigValue[A]).as[ConfigValue]

  implicit lazy val configNumberGen: Gen[ConfigValue :@ Number] =
    Gen.oneOf(
      Gen[ConfigValue :@ Byte].untag,
      Gen[ConfigValue :@ Int].untag,
      Gen[ConfigValue :@ Long].untag,
      Gen[ConfigValue :@ Double].untag
    ).tag[Number]

  implicit def genConfigList0[A: Gen : ToConfigValue]: Gen[ConfigList :@ A] =
    Gen.list(genConfigValue0[A]).map(_.asJava).map(ConfigValueFactory.fromIterable).tag[A]

  def genConfigList[A: ToConfigValue](g: Gen[A]): Gen[ConfigList] =
    genConfigList0(g, ToConfigValue[A]).untag

  def genNonEmptyConfigList[A: ToConfigValue](g: Gen[A]): Gen[ConfigList] = {
    val cg = genConfigValue(g)
    Apply[Gen].apply2(cg, Gen.list(cg))(_ :: _).map(_.asJava).map(ConfigValueFactory.fromIterable)
  }

  implicit lazy val configListGen: Gen[ConfigList] =
    genConfigList(configValueGen)

  implicit lazy val configValueJListGen: Gen[ju.List[ConfigValue]] =
    Gen[ConfigList].map(cl => cl)

  implicit lazy val configObjectGen: Gen[ConfigObject] =
    Gen.mapGen(Gen[String], configValueGen).map(_.asJava).map(ConfigValueFactory.fromMap)

  implicit lazy val configValueJavaMapGen: Gen[ju.Map[String, ConfigValue]] =
    Gen[ConfigObject].map(co => co)

  implicit lazy val configValueGen: Gen[ConfigValue] =
    Gen.lazyFrequency(
      40 -> Need(Gen[ConfigValue :@ String].untag),
      40 -> Need(Gen[ConfigValue :@ Number].untag),
      10 -> Need(Gen[ConfigValue :@ Boolean].untag),
      5 -> Need(configListGen.as[ConfigValue]),
      5 -> Need(configObjectGen.as[ConfigValue])
    ).mapSize(_ / 2)

  implicit lazy val configGen: Gen[Config] =
    configObjectGen.map(_.toConfig)


  implicit def arrayEqual[A: Equal]: Equal[Array[A]] =
    Equal.equalBy(_.toList)

  implicit def mapSubEqual[M[_, _], A: Order, B: Equal](implicit ev: M[A, B] <:< collection.Map[A, B]): Equal[M[A, B]] =
    Equal.equalBy(_.toMap)

  implicit def setEqual[A: Equal]: Equal[Set[A]] =
    Equal.equalA[Set[A]]

  implicit def javaIterableEqual[F[_], A: Equal](implicit ev: F[A] <:< jl.Iterable[A]): Equal[F[A]] =
    Equal.equalBy(ev(_).asScala.toList)

  implicit def javaMapEqual[A: Order, B: Equal]: Equal[ju.Map[A, B]] =
    Equal.equalBy(_.asScala.toMap)

  implicit def javaSetEqual[A: Equal]: Equal[ju.Set[A]] =
    Equal.equalBy(_.asScala.toSet)

  implicit lazy val javaByteEqual: Equal[jl.Byte] =
    Equal.equalBy(_.byteValue())

  implicit lazy val javaShortEqual: Equal[jl.Short] =
    Equal.equalBy(_.shortValue())

  implicit lazy val javaIntegerEqual: Equal[jl.Integer] =
    Equal.equalBy(_.intValue())

  implicit lazy val javaLongEqual: Equal[jl.Long] =
    Equal.equalBy(_.longValue())

  implicit lazy val javaFloatEqual: Equal[jl.Float] =
    Equal.equalBy(_.floatValue())

  implicit lazy val javaDoubleEqual: Equal[jl.Double] =
    Equal.equalBy(_.doubleValue())

  implicit lazy val javaCharacterEqual: Equal[jl.Character] =
    Equal.equalBy(_.charValue())

  implicit lazy val javaBooleanEqual: Equal[jl.Boolean] =
    Equal.equalBy(_.booleanValue())

  implicit lazy val javaDurationEqual: Equal[jt.Duration] =
    Equal.equalA[jt.Duration]

  implicit lazy val symbolOrder: Order[Symbol] =
    Order[String].contramap(_.name)

  implicit lazy val configEqual: Equal[Config] =
    Equal.equalA[Config]

  implicit lazy val configValueEqual: Equal[ConfigValue] =
    Equal.equalA[ConfigValue]

  implicit lazy val configObjectEqual: Equal[ConfigObject] =
    Equal.equalA[ConfigObject]

  implicit lazy val configListEqual: Equal[ConfigList] =
    Equal.equalA[ConfigList]

  implicit lazy val configMemorySizeEqual: Equal[ConfigMemorySize] =
    Equal.equalA[ConfigMemorySize]

}
