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

package configs.instance

import com.typesafe.config.ConfigFactory
import configs.Configs
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.collection._
import configs.testutil.instance.string._
import java.{lang => jl}
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}

object BasicTypesTest extends Scalaprops {

  val byte = check[Byte]
  val byteError = integralError[Byte](Byte.MinValue - 1, Byte.MaxValue + 1)

  val javaByte = check[jl.Byte]

  val short = check[Short]
  val shortError = integralError[Short](Short.MinValue - 1, Short.MaxValue + 1)

  val javaShort = check[jl.Short]

  val int = check[Int]
  val intError = integralError[Int](Int.MinValue - 1L, Int.MaxValue + 1L)

  val javaInteger = check[jl.Integer]

  val long = check[Long]
  val longError = integralError[Long](BigInt(Long.MinValue) - 1, BigInt(Long.MaxValue) + 1)

  val javaLong = check[jl.Long]

  val float = check[Float]

  val javaFloat = check[jl.Float]

  val double = check[Double]

  val javaDouble = check[jl.Double]

  val boolean = check[Boolean]

  val javaBoolean = check[jl.Boolean]

  val character = check[Char]

  val characterList = check[List[Char]]

  val javaCharacter = check[jl.Character]

  val javaCharacterList = check[List[jl.Character]]

  val string = check[String]


  def integralError[A](minM1: BigInt, maxP1: BigInt)(implicit A: Configs[A]) = {
    val big = "9999999999999999999"
    val config = ConfigFactory.parseString(
      s"""nan = NaN
          |infinite = Infinity
          |min-m1 = $minM1
          |max-p1 = $maxP1
          |too-big = $big
          |""".stripMargin)
    def p(id: String)(path: String, msg: String): Properties[String] =
      forAll {
        val a = A.get(config, path)
        val result = a.failed.map(_.head).exists { e =>
          e.paths == List(path) && e.message.contains(msg)
        }
        if (!result) println(s"\n$a")
        result
      }.toProperties(id)
    Properties.list(
      p("not a number")("nan", "NaN"),
      p("infinite value")("infinite", "Infinity"),
      p("min value minus 1")("min-m1", minM1.toString),
      p("max value plus 1")("max-p1", maxP1.toString),
      p("too big value")("too-big", big)
    )
  }

}
