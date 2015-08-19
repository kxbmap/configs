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

import com.typesafe.config.{Config, ConfigList, ConfigMemorySize, ConfigObject, ConfigUtil, ConfigValue, ConfigValueFactory}
import java.{lang => jl, time => jt, util => ju}
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.{Gen, Property}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.{Equal, Need}

package object testkit {

  val q = ConfigUtil.quoteString _


  implicit class UtilOps[A](private val self: A) {

    def configValue(implicit A: ConfigVal[A]): ConfigValue = A.configValue(self)

    def cv(implicit A: ConfigVal[A]): ConfigValue = configValue
  }


  private[testkit] def intercept0(block: => Unit)(cond: PartialFunction[Throwable, Boolean]): Boolean =
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


  implicit def javaListGen[A: Gen]: Gen[ju.List[A]] =
    Gen.list[A].map(_.asJava)

  implicit def javaStringMapGen[A: Gen]: Gen[ju.Map[String, A]] =
    Gen[Map[String, A]].map(_.asJava)

  implicit def javaSymbolMapGen[A: Gen]: Gen[ju.Map[Symbol, A]] =
    Gen[Map[String, A]].map(_.map(t => Symbol(t._1) -> t._2).asJava)

  implicit def javaSetGen[A: Gen]: Gen[ju.Set[A]] =
    Gen[Set[A]].map(_.asJava)


  implicit lazy val stringGen: Gen[String] =
    Gen.parameterised { (size, r) =>
      import jl.{Character => C}
      val cp = Gen.chooseR(C.MIN_CODE_POINT, C.MAX_CODE_POINT, r)
      Gen.sequenceNArray(size, cp).map { cps =>
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

  implicit lazy val javaDurationConfigVal: ConfigVal[jt.Duration] =
    ConfigVal[String].contramap(d => s"${d.toNanos}ns")


  implicit lazy val configMemorySizeGen: Gen[ConfigMemorySize] =
    Gen.nonNegativeLong.map(ConfigMemorySize.ofBytes)


  private def genConfigValue[A: Gen]: Gen[ConfigValue] =
    Gen[A].map(ConfigValueFactory.fromAnyRef)

  lazy val configStringGen: Gen[ConfigValue] =
    genConfigValue[String]

  lazy val configNumberGen: Gen[ConfigValue] =
    Gen.oneOf(
      genConfigValue[Byte],
      genConfigValue[Int],
      genConfigValue[Long],
      genConfigValue[Double]
    )

  lazy val configBooleanGen: Gen[ConfigValue] =
    Gen.elements(
      ConfigValueFactory.fromAnyRef(true),
      ConfigValueFactory.fromAnyRef(false)
    )

  implicit lazy val configListGen: Gen[ConfigList] =
    Gen.list(configValueGen).map(xs => ConfigValueFactory.fromIterable(xs.asJava))

  implicit lazy val configValueJavaListGen: Gen[ju.List[ConfigValue]] =
    Gen[ConfigList].map(cl => cl)

  implicit lazy val configObjectGen: Gen[ConfigObject] =
    Gen.mapGen(Gen[String], configValueGen).map(m => ConfigValueFactory.fromMap(m.asJava))

  implicit lazy val configValueJavaMapGen: Gen[ju.Map[String, ConfigValue]] =
    Gen[ConfigObject].map(co => co)

  implicit lazy val configValueGen: Gen[ConfigValue] =
    Gen.lazyFrequency(
      //      1 -> Need(Gen.value(ConfigValueFactory.fromAnyRef(null))),
      40 -> Need(configStringGen),
      40 -> Need(configNumberGen),
      10 -> Need(configBooleanGen),
      5 -> Need(configListGen.asInstanceOf[Gen[ConfigValue]]),
      5 -> Need(configObjectGen.asInstanceOf[Gen[ConfigValue]])
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
