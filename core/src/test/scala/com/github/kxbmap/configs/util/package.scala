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

import com.typesafe.config.{Config, ConfigFactory, ConfigList, ConfigMemorySize, ConfigObject, ConfigUtil, ConfigValue, ConfigValueFactory}
import java.{lang => jl, time => jt, util => ju}
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}
import scalaprops.Or.Empty
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{:-:, Gen, Properties, Property}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.string._
import scalaz.{Apply, Equal, Need, Order}


package object util {

  type :@[A, T] = A { type Tag = T }

  implicit class TagOps[F[_], A](private val g: F[A]) extends AnyVal {
    def tag[T]: F[A :@ T] = g.asInstanceOf[F[A :@ T]]
  }

  implicit class UntagOps[F[_], A, T](private val g: F[A :@ T]) extends AnyVal {
    def untag: F[A] = g.asInstanceOf[F[A]]
  }


  val q = ConfigUtil.quoteString _


  def hideConfigs[A: ClassTag]: Configs[A] = (_, _) => sys.error(s"hiding Configs[${classTag[A]}] used")


  def check[A: Configs : Gen : Equal : ToConfigValue : IsMissing : WrongTypeValue : IsWrongType : BadValue : IsBadValue]: Properties[Unit :-: String :-: Empty] =
    checkId(())

  def check[A: Configs : Gen : Equal : ToConfigValue : IsMissing : WrongTypeValue : IsWrongType : BadValue : IsBadValue](id: String): Properties[String :-: String :-: Empty] =
    checkId(id)

  private def checkId[A: Order, B: Configs : Gen : Equal : ToConfigValue : IsMissing : WrongTypeValue : IsWrongType : BadValue : IsBadValue](id: A) =
    Properties.either(
      id,
      checkGet[B].toProperties("get"),
      Seq(checkMissing[B].toProperties("missing")) ++
        checkWrongType[B].map(_.toProperties("wrong type")) ++
        checkBadValue[B].map(_.toProperties("bad value")): _*
    )

  private def checkGet[A: Configs : Gen : Equal : ToConfigValue] = forAll { value: A =>
    Equal[A].equal(Configs[A].extract(value.toConfigValue), value)
  }

  private def checkMissing[A: Configs : IsMissing] = forAll {
    val p = "missing"
    val c = ConfigFactory.empty()
    IsMissing[A].check(Need(Configs[A].get(c, p)))
  }

  private def checkWrongType[A: Configs : IsWrongType : WrongTypeValue] = WrongTypeValue[A].gen.map {
    forAllG(_) { cv =>
      IsWrongType[A].check(Need(Configs[A].extract(cv)))
    }
  }

  private def checkBadValue[A: Configs : BadValue : IsBadValue] = BadValue[A].gen.map {
    forAllG(_) { cv =>
      IsBadValue[A].check(Need(Configs[A].extract(cv)))
    }
  }


  implicit class ToConfigValOps[A](private val self: A) {
    def toConfigValue(implicit A: ToConfigValue[A]): ConfigValue = A.toConfigValue(self)
  }


  private[util] def intercept0(block: => Unit)(cond: PartialFunction[Throwable, Boolean]): Boolean =
    try {
      block
      false
    } catch cond

  def intercept[A](block: => A)(cond: PartialFunction[Throwable, Boolean]): Property =
    forAll {
      intercept0(block)(cond)
    }

  def intercept[R, A1: Gen](block: A1 => R)(cond: PartialFunction[Throwable, Boolean]): Property =
    forAll { a1: A1 =>
      intercept0(block(a1))(cond)
    }

  def intercept[R, A1: Gen, A2: Gen](block: (A1, A2) => R)(cond: PartialFunction[Throwable, Boolean]): Property =
    forAll { (a1: A1, a2: A2) =>
      intercept0(block(a1, a2))(cond)
    }


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

  implicit lazy val javaDurationToConfigValue: ToConfigValue[jt.Duration] =
    ToConfigValue[String].contramap(d => s"${d.toNanos}ns")


  implicit lazy val configMemorySizeGen: Gen[ConfigMemorySize] =
    Gen.nonNegativeLong.map(ConfigMemorySize.ofBytes)


  implicit def genConfigValue[A: Gen : ToConfigValue]: Gen[ConfigValue :@ A] =
    Gen[A].map(_.toConfigValue).tag[A]

  implicit lazy val configNumberGen: Gen[ConfigValue :@ Number] =
    Gen.oneOf(
      Gen[ConfigValue :@ Byte].untag,
      Gen[ConfigValue :@ Int].untag,
      Gen[ConfigValue :@ Long].untag,
      Gen[ConfigValue :@ Double].untag
    ).tag[Number]

  implicit def genConfigList0[A](implicit g: Gen[ConfigValue :@ A]): Gen[ConfigList :@ A] =
    Gen.list(g).map(_.asJava |> ConfigValueFactory.fromIterable).tag[A]

  def genConfigList[A](g: Gen[ConfigValue]): Gen[ConfigList] =
    genConfigList0(g.tag[A]).untag

  def genNonEmptyConfigList[A](g: Gen[ConfigValue]): Gen[ConfigList] =
    Apply[Gen].apply2(g, Gen.list(g))(_ :: _).map(_.asJava |> ConfigValueFactory.fromIterable)

  implicit lazy val configListGen: Gen[ConfigList] =
    genConfigList(configValueGen)

  implicit lazy val configValueJListGen: Gen[ju.List[ConfigValue]] =
    Gen[ConfigList].map(cl => cl)

  implicit lazy val configObjectGen: Gen[ConfigObject] =
    Gen.mapGen(Gen[String], configValueGen).map(_.asJava |> ConfigValueFactory.fromMap)

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

  implicit def mapEqual[A: Equal, B: Equal]: Equal[Map[A, B]] =
    Equal.equalA[Map[A, B]]

  implicit def setEqual[A: Equal]: Equal[Set[A]] =
    Equal.equalA[Set[A]]

  implicit def javaListEqual[A: Equal]: Equal[ju.List[A]] =
    Equal.equalBy(_.asScala.toList)

  implicit def javaMapEqual[A: Equal, B: Equal]: Equal[ju.Map[A, B]] =
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

  implicit lazy val symbolEqual: Equal[Symbol] =
    Equal.equalA[Symbol]

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
