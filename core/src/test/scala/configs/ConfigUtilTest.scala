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

import configs.testutil.instance.string._
import scalaprops.Property.forAllG
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.NonEmptyList
import scalaz.syntax.apply._

object ConfigUtilTest extends Scalaprops {

  private val lower: Gen[String] = Gen.nonEmptyString(Gen.alphaLowerChar)
  private val UPPER: Gen[String] = Gen.nonEmptyString(Gen.alphaUpperChar)
  private val Camel: Gen[String] = (Gen.alphaUpperChar |@| lower)(_ +: _)
  private val Number: Gen[String] = Gen.nonEmptyString(Gen.numChar)

  private def tc(f: (String, String, String) => String)(a: String, b: String, c: String): (String, List[String]) =
    (f(a, b, c), List(a, b, c))

  private def concat = tc(_ + _ + _) _
  private def hyphen = tc(_ + "-" + _ + "-" + _) _
  private def snake = tc(_ + "_" + _ + "_" + _) _

  val splitWords = {
    val props = NonEmptyList(
      "lower-hyphen-case" -> (lower |@| lower |@| lower)(hyphen),
      "lowerCamelCase" -> (lower |@| Camel |@| Camel)(concat),
      "UpperCamelCase" -> (Camel |@| Camel |@| Camel)(concat),
      "lower_snake_case" -> (lower |@| lower |@| lower)(snake),
      "UPPER_SNAKE_CASE" -> (UPPER |@| UPPER |@| UPPER)(snake),
      "UPPERThenCamel" -> (UPPER |@| Camel |@| Camel)(concat),
      "camelThenUPPER" -> (lower |@| Camel |@| UPPER)(concat),
      "lowerNumberLower" -> (lower |@| Number |@| lower)(concat),
      "UPPERNumberUPPER" -> (UPPER |@| Number |@| UPPER)(concat),
      "CamelNumberCamel" -> (Camel |@| Number |@| Camel)(concat),
      "lowerThenNumber" -> (lower |@| Camel |@| Number)(concat),
      "lowerUPPERNumber" -> (lower |@| UPPER |@| Number)(concat)
    ).map {
      case (id, g) =>
        forAllG(g) {
          case (s, expected) => ConfigUtil.splitWords(s) == expected
        }.toProperties(id)
    }
    Properties.list(props.head, props.tail.toList: _*)
  }

}
