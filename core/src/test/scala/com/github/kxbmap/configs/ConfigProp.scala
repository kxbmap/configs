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

import com.github.kxbmap.configs.testkit._
import com.typesafe.config.{Config, ConfigFactory, ConfigList, ConfigMemorySize, ConfigObject, ConfigValue, ConfigValueFactory}
import java.{lang => jl, time => jt, util => ju}
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}
import scalaprops.Or.Empty
import scalaprops.Property.forAll
import scalaprops.{:-:, Gen, Properties}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.string._
import scalaz.{Equal, Need, Order}

trait ConfigProp {

  def hideConfigs[A: ClassTag]: Configs[A] = (_, _) => sys.error(s"hiding Configs[${classTag[A]}] used")


  def check[A: Configs : Gen : Equal : ConfigVal : IsMissing : IsWrongType : WrongTypeValue]: Properties[Unit :-: String :-: Empty] =
    checkId(())

  def check[A: Configs : Gen : Equal : ConfigVal : IsMissing : IsWrongType : WrongTypeValue](id: String): Properties[String :-: String :-: Empty] =
    checkId(id)

  private def checkId[A: Order, B: Configs : Gen : Equal : ConfigVal : IsMissing : IsWrongType : WrongTypeValue](id: A) =
    Properties.either(
      id,
      checkGet[B].toProperties("get"),
      checkMissing[B].toProperties("missing"),
      checkWrongType[B].toProperties("wrong type")
    )

  private def checkGet[A: Configs : Gen : Equal : ConfigVal] = forAll { value: A =>
    Equal[A].equal(Configs[A].extract(value.cv), value)
  }

  private def checkMissing[A: Configs : IsMissing] = forAll {
    val p = "missing"
    val c = ConfigFactory.empty()
    IsMissing[A].check(Need(Configs[A].get(c, p)))
  }

  private def checkWrongType[A: Configs : IsWrongType : WrongTypeValue] = forAll {
    val p = "dummy-path"
    val c = ConfigValueFactory.fromAnyRef(WrongTypeValue[A].value).atKey(p)
    IsWrongType[A].check(Need(Configs[A].get(c, p)))
  }


  implicit def generalEqual[A]: Equal[A] =
    Equal.equalA[A]

  implicit def arrayEqual[A: Equal]: Equal[Array[A]] =
    Equal.equalBy(_.toList)


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
        def copyChars(i: Int, j: Int, arr: Array[Char]): Array[Char] =
          if (i < cps.length) {
            val cs = C.toChars(cps(i))
            System.arraycopy(cs, 0, arr, j, cs.length)
            copyChars(i + 1, j + cs.length, arr)
          } else {
            require(j == arr.length)
            arr
          }
        val cc = cps.foldLeft(0)(_ + C.charCount(_))
        new String(copyChars(0, 0, new Array(cc)))
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

}
