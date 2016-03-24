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

import java.{math => jm}
import scalaz.{Equal, std}

object math {

  implicit lazy val bigIntEqual: Equal[BigInt] =
    std.math.bigInt.bigIntInstance

  implicit lazy val bigDecimalEqual: Equal[BigDecimal] =
    std.math.bigDecimal.bigDecimalInstance

  implicit lazy val javaBigIntegerEqual: Equal[jm.BigInteger] =
    std.java.math.bigInteger.bigIntegerInstance

  implicit lazy val javaBigDecimalEqual: Equal[jm.BigDecimal] =
    Equal.equalA[jm.BigDecimal]

}
