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

package configs.macros

import configs.testutil.instance.string._
import java.util.Locale
import scalaprops.Property.forAllG
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.NonEmptyList
import scalaz.syntax.apply._

object UtilTest extends Scalaprops with Util {

  private val lower: Gen[String] = Gen.nonEmptyString(Gen.alphaLowerChar)
  private val UPPER: Gen[String] = Gen.nonEmptyString(Gen.alphaUpperChar)
  private val Camel: Gen[String] = (Gen.alphaUpperChar |@| lower)(_ +: _)
  private val Number: Gen[String] = Gen.genString(Gen.numChar, 1)

  private def tc(f: (String, String, String) => String)(a: String, b: String, c: String): (String, String) =
    f(a, b, c) -> Seq(a, b, c).map(_.toLowerCase(Locale.ROOT)).mkString("-")

  private def concat = tc(_ + _ + _) _
  private def hyphen = tc(_ + "-" + _ + "-" + _) _
  private def snake = tc(_ + "_" + _ + "_" + _) _

  val `Format to lower-hyphen-case` = {
    val props = NonEmptyList(
      "from lower-hyphen-case" -> (lower |@| lower |@| lower)(hyphen),
      "from lowerCamelCase" -> (lower |@| Camel |@| Camel)(concat),
      "from UpperCamelCase" -> (Camel |@| Camel |@| Camel)(concat),
      "from lower_snake_case" -> (lower |@| lower |@| lower)(snake),
      "from UPPER_SNAKE_CASE" -> (UPPER |@| UPPER |@| UPPER)(snake),
      "from UPPERThenCamel" -> (UPPER |@| Camel |@| Camel)(concat),
      "from camelThenUPPER" -> (lower |@| Camel |@| UPPER)(concat),
      "from lowerNumberLower" -> (lower |@| Number |@| lower)(concat),
      "from UPPERNumberUPPER" -> (UPPER |@| Number |@| UPPER)(concat),
      "from CamelNumberCamel" -> (Camel |@| Number |@| Camel)(concat),
      "from lowerThenNumber" -> (lower |@| Camel |@| Number)(concat),
      "from lowerUPPERNumber" -> (lower |@| UPPER |@| Number)(concat)
    ).map {
      case (id, g) =>
        forAllG(g) {
          case (s, expected) => toLowerHyphenCase(s) == expected
        }.toProperties(id)
    }
    Properties.list(props.head, props.tail.toList: _*)
  }

}
