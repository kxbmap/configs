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

package configs.testutil.instance

import java.{lang => jl}
import scalaprops.Gen
import scalaz.{Order, Ordering, std}

object anyVal {

  implicit lazy val unitOrder: Order[Unit] = std.anyVal.unitInstance

  implicit lazy val byteOrder: Order[Byte] = std.anyVal.byteInstance
  implicit lazy val shortOrder: Order[Short] = std.anyVal.shortInstance
  implicit lazy val intOrder: Order[Int] = std.anyVal.intInstance
  implicit lazy val longOrder: Order[Long] = std.anyVal.longInstance
  implicit lazy val doubleOrder: Order[Double] = Order.order { (a, b) =>
    jl.Double.compare(a, b) match {
      case -1 if a != b => Ordering.LT
      case 1 if a != b => Ordering.GT
      case _ => Ordering.EQ
    }
  }
  implicit lazy val floatOrder: Order[Float] = Order.order { (a, b) =>
    jl.Float.compare(a, b) match {
      case -1 if a != b => Ordering.LT
      case 1 if a != b => Ordering.GT
      case _ => Ordering.EQ
    }
  }
  implicit lazy val charOrder: Order[Char] = std.anyVal.char
  implicit lazy val booleanOrder: Order[Boolean] = std.anyVal.booleanInstance

  implicit lazy val javaByteOrder: Order[jl.Byte] = Order.orderBy(_.byteValue())
  implicit lazy val javaShortOrder: Order[jl.Short] = Order.orderBy(_.shortValue())
  implicit lazy val javaIntegerOrder: Order[jl.Integer] = Order.orderBy(_.intValue())
  implicit lazy val javaLongOrder: Order[jl.Long] = Order.orderBy(_.longValue())
  implicit lazy val javaDoubleOrder: Order[jl.Double] = Order.orderBy(_.doubleValue())
  implicit lazy val javaFloatOrder: Order[jl.Float] = Order.orderBy(_.floatValue())
  implicit lazy val javaCharacterOrder: Order[jl.Character] = Order.orderBy(_.charValue())
  implicit lazy val javaBooleanOrder: Order[jl.Boolean] = Order.orderBy(_.booleanValue())

  implicit lazy val javaNumberGen: Gen[jl.Number] =
    Gen.oneOf(
      Gen[jl.Byte].widen[jl.Number],
      Gen[jl.Short].widen[jl.Number],
      Gen[jl.Integer].widen[jl.Number],
      Gen[jl.Long].widen[jl.Number],
      Gen[jl.Float].widen[jl.Number],
      Gen[jl.Double].widen[jl.Number]
    )

  lazy val infiniteDoubleGen: Gen[Double] =
    Gen.elements(Double.PositiveInfinity, Double.NegativeInfinity)

  implicit lazy val charGen: Gen[Char] = {
    import jl.{Character => C}
    Gen.choose(C.MIN_VALUE, C.MAX_VALUE).map(C.toChars(_)(0))
  }

  implicit lazy val javaCharacterGen: Gen[jl.Character] =
    charGen.map(Char.box)

}
