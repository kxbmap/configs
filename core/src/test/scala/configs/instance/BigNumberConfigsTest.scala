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

import configs.Configs
import configs.util._
import java.{math => jm}
import scalaprops.Property.forAll
import scalaprops.Scalaprops
import scalaz.Equal
import scalaz.std.java.math.bigInteger._
import scalaz.std.math.bigDecimal._
import scalaz.std.math.bigInt._
import scalaz.syntax.equal._

object BigNumberConfigsTest extends Scalaprops {

  val bigInt = check[BigInt]

  val bigIntFromDecimal = forAll { d: BigDecimal =>
    Configs[BigInt].extractValue(d.toConfigValue).exists(_ === d.toBigInt())
  }

  val bigInteger = check[jm.BigInteger]

  val bigIntegerFromDecimal = forAll { d: jm.BigDecimal =>
    Configs[jm.BigInteger].extractValue(d.toConfigValue).exists(_ === d.toBigInteger)
  }

  val bigDecimal = check[BigDecimal]

  val javaBigDecimal = check[jm.BigDecimal]


  implicit lazy val javaBigDecimalEqual: Equal[jm.BigDecimal] =
    Equal.equalA[jm.BigDecimal]

}
