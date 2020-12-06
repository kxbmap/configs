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

package configs.syntax

import configs.ConfigMemorySize
import configs.testutil.instance.config._
import configs.testutil.instance.math._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Property, Scalaprops}
import scalaz.syntax.std.boolean._

object RichConfigMemorySizeTest extends Scalaprops {

  private def forAllWith[A](gen: Gen[A])(f: (ConfigMemorySize, A) => Boolean): Property =
    forAllG(Gen[ConfigMemorySize], gen)(f)

  val + =
    forAll { (a: ConfigMemorySize, b: ConfigMemorySize) =>
      a + b == ConfigMemorySize(a.value + b.value)
    }

  val - =
    forAll { (a: ConfigMemorySize, b: ConfigMemorySize) =>
      (a.value >= b.value) --> {
        a - b == ConfigMemorySize(a.value - b.value)
      }
    }

  val `*` = {
    Properties.properties("by")(
      "non-neg value" -> forAllWith(nonNegativeBigInt) { (m, n) =>
        m * n == ConfigMemorySize(m.value * n)
      },
      "negative value" -> forAllWith(negativeBigInt) { (m, n) =>
        if (m == ConfigMemorySize.Zero)
          m * n == ConfigMemorySize.Zero
        else try {
          m * n
          sys.error(s"$m * $n")
        } catch {
          case e: IllegalArgumentException => e.getMessage.contains((m.value * n).toString)
        }
      })
  }

  val `/` = Properties.properties("by")(
    "positive value" -> forAllWith(positiveBigInt) { (m, n) =>
      m / n == ConfigMemorySize(m.value / n)
    },
    "negative value" -> forAllWith(negativeBigInt) { (m, n) =>
      if (m == ConfigMemorySize.Zero)
        m / n == ConfigMemorySize.Zero
      else try {
        m / n
        sys.error(s"$m / $n")
      } catch {
        case e: IllegalArgumentException => e.getMessage.contains(n.toString)
      }
    },
    "zero" -> forAll { a: ConfigMemorySize =>
      try {
        a / 0
        sys.error(s"$a / 0")
      } catch {
        case _: ArithmeticException => true
      }
    })

  val `<<` =
    forAllWith(Gen[Int]) { (m, n) =>
      (try {
        m.value << n
        false
      } catch {
        case _: ArithmeticException =>
          try {
            m << n
            sys.error(s"$m << $n")
          } catch {
            case _: ArithmeticException => true
          }
      }) ||
        (m << n) == ConfigMemorySize(m.value << n)
    }

  val `>>` =
    forAllWith(Gen[Int]) { (m, n) =>
      (try {
        m.value >> n
        false
      } catch {
        case _: ArithmeticException =>
          try {
            m >> n
            sys.error(s"$m >> $n")
          } catch {
            case _: ArithmeticException => true
          }
      }) ||
        (m >> n) == ConfigMemorySize(m.value >> n)
    }

}
