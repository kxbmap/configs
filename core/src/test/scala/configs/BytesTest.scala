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

import configs.testutil.fun._
import configs.testutil.instance.bytes._
import configs.testutil.instance.string._
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}
import scalaz.Monoid
import scalaz.syntax.std.boolean._


object BytesTest extends Scalaprops {

  val bytes = check[Bytes]

  val ordering = forAll { bs: List[Bytes] =>
    bs.sorted == bs.map(_.value).sorted.map(Bytes.apply)
  }

  val ordered = forAll { (l: Bytes, r: Bytes) =>
    (l compare r) == (l.value compare r.value)
  }

  val plus = forAll { (l: Bytes, r: Bytes) =>
    l + r == Bytes(l.value + r.value)
  }

  val minus = forAll { (l: Bytes, r: Bytes) =>
    l - r == Bytes(l.value - r.value)
  }

  val multiply = Properties.properties("by")(
    "int" -> forAll { (l: Bytes, r: Int) =>
      l * r == Bytes(l.value * r)
    },
    "long" -> forAll { (l: Bytes, r: Long) =>
      l * r == Bytes(l.value * r)
    },
    "double" -> forAll { (l: Bytes, r: Double) =>
      l * r == Bytes((l.value * r).toLong)
    }) x
    Properties.properties("to")(
      "double" -> forAll { (l: Double, r: Bytes) =>
        l * r == Bytes((l * r.value).toLong)
      })

  val divide = Properties.properties("by")(
    "int" -> forAll { (l: Bytes, r: Int) =>
      (r != 0L) --> {
        l / r == Bytes(l.value / r)
      }
    },
    "long" -> forAll { (l: Bytes, r: Long) =>
      (r != 0L) --> {
        l / r == Bytes(l.value / r)
      }
    },
    "double" -> forAll { (l: Bytes, r: Double) =>
      l / r == Bytes((l.value / r).toLong)
    },
    "bytes" -> forAll { (l: Bytes, r: Bytes) =>
      l / r == l.value.toDouble / r.value.toDouble
    })

  val unary_- = forAll { b: Bytes =>
    -b == Bytes(-b.value)
  }

  val unary_+ = forAll { b: Bytes =>
    +b == b
  }

  val `<<` = Properties.properties("by")(
    "int" -> forAll { (a: Bytes, b: Int) =>
      (a << b) == Bytes(a.value << b)
    },
    "long" -> forAll { (a: Bytes, b: Long) =>
      (a << b) == Bytes(a.value << b)
    })

  val `>>` = Properties.properties("by")(
    "int" -> forAll { (a: Bytes, b: Int) =>
      (a >> b) == Bytes(a.value >> b)
    },
    "long" -> forAll { (a: Bytes, b: Long) =>
      (a >> b) == Bytes(a.value >> b)
    })

  val `>>>` = Properties.properties("by")(
    "int" -> forAll { (a: Bytes, b: Int) =>
      (a >>> b) == Bytes(a.value >>> b)
    },
    "long" -> forAll { (a: Bytes, b: Long) =>
      (a >>> b) == Bytes(a.value >>> b)
    })

  val `+/0 monoid` = {
    implicit val m: Monoid[Bytes] = Monoid.instance(_ + _, Bytes(0))
    scalaprops.scalazlaws.monoid.all[Bytes]
  }

}
