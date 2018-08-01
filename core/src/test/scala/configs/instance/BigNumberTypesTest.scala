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

import configs.testutil.fun._
import configs.testutil.instance.math._
import configs.{ConfigReader, ConfigWriter}
import java.{math => jm}
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object BigNumberTypesTest extends Scalaprops {

  val bigInt = check[BigInt]

  val bigIntFromDecimal = forAll { d: BigDecimal =>
    ConfigReader[BigInt].extractValue(ConfigWriter[BigDecimal].write(d)).contains(d.toBigInt)
  }

  val bigInteger = check[jm.BigInteger]

  val bigIntegerFromDecimal = forAll { d: jm.BigDecimal =>
    ConfigReader[jm.BigInteger].extractValue(ConfigWriter[BigDecimal].write(d)).contains(d.toBigInteger)
  }

  val bigDecimal = check[BigDecimal]

  val javaBigDecimal = check[jm.BigDecimal]

}
