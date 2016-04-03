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

import configs.testutil.instance.anyVal._
import configs.testutil.instance.config._
import configs.testutil.instance.string._
import configs.{Bytes, MemorySize}
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Property, Scalaprops}
import scalaz.syntax.std.boolean._

object EnrichMemorySizeTest extends Scalaprops {

  private def forAllWith[A](gen: Gen[A])(f: (MemorySize, A) => Boolean): Property =
    forAllG(Gen[MemorySize], gen)(f)

  val + = forAll { (a: MemorySize, b: MemorySize) =>
    if (a.value + b.value >= 0L) {
      a + b == MemorySize(a.value + b.value)
    } else try {
      a + b
      sys.error(s"$a + $b")
    } catch {
      case _: ArithmeticException => true
    }
  }

  val - = forAllWith(Gen[MemorySize]) { (a, b) =>
    (a.value >= b.value) --> {
      a - b == MemorySize(a.value - b.value)
    }
  }

  val `*` = {
    def isExact(x: MemorySize, y: Long): Boolean =
      try {
        Math.multiplyExact(x.value, y)
        true
      } catch {
        case _: ArithmeticException => false
      }
    Properties.properties("by")(
      "non-neg int" -> forAllWith(Gen.nonNegativeInt) { (a, b) =>
        isExact(a, b) --> {
          a * b == MemorySize(a.value * b)
        }
      },
      "negative int" -> forAllWith(Gen.negativeInt) { (a, b) =>
        if (a.value == 0L) {
          a * b == MemorySize.Zero
        } else try {
          a * b
          sys.error(s"$a * $b")
        } catch {
          case e: IllegalArgumentException => e.getMessage.contains(b.toString)
        }
      },
      "non-neg long" -> forAllWith(Gen.nonNegativeLong) { (a, b) =>
        isExact(a, b) --> {
          a * b == MemorySize(a.value * b)
        }
      },
      "negative long" -> forAllWith(Gen.negativeLong) { (a, b) =>
        if (a.value == 0L) {
          a * b == MemorySize.Zero
        } else try {
          a * b
          sys.error(s"$a * $b")
        } catch {
          case e: IllegalArgumentException => e.getMessage.contains(b.toString)
        }
      },
      "non-neg double" -> forAllWith(Gen.nonNegativeFiniteDouble) { (a, b) =>
        if (a.value * b <= Long.MaxValue) {
          a * b == MemorySize((a.value * b).toLong)
        } else try {
          a * b
          sys.error(s"$a * $b")
        } catch {
          case _: ArithmeticException => true
        }
      },
      "negative double" -> forAllWith(Gen.negativeFiniteDouble) { (a, b) =>
        if (a.value == 0L || java.lang.Double.compare(b, -0d) == 0) {
          a * b == MemorySize.Zero
        } else try {
          a * b
          sys.error(s"$a * $b")
        } catch {
          case e: IllegalArgumentException => e.getMessage.contains(b.toString)
        }
      },
      "infinite double" -> forAllWith(infiniteDoubleGen) { (a, b) =>
        try {
          a * b
          sys.error(s"$a * $b")
        } catch {
          case e: IllegalArgumentException => e.getMessage.contains(b.toString)
        }
      },
      "NaN" -> forAll { a: MemorySize =>
        try {
          a * Double.NaN
          sys.error(s"$a * ${Double.NaN}")
        } catch {
          case e: IllegalArgumentException => e.getMessage.contains("NaN")
        }
      })
  }

  val `/` = Properties.properties("by")(
    "positive int" -> forAllWith(Gen.positiveInt) { (a, b) =>
      a / b == MemorySize(a.value / b)
    },
    "negative int" -> forAllWith(Gen.negativeInt) { (a, b) =>
      if (a.value == 0L) {
        a / b == MemorySize.Zero
      } else try {
        a / b
        sys.error(s"$a / $b")
      } catch {
        case e: IllegalArgumentException => e.getMessage.contains(b.toString)
      }
    },
    "zero int" -> forAll { a: MemorySize =>
      try {
        a / 0
        sys.error(s"$a / 0")
      } catch {
        case _: ArithmeticException => true
      }
    },
    "positive long" -> forAllWith(Gen.positiveLong) { (a, b) =>
      a / b == MemorySize(a.value / b)
    },
    "negative long" -> forAllWith(Gen.negativeLong) { (a, b) =>
      if (a.value == 0L) {
        a / b == MemorySize.Zero
      } else try {
        a / b
        sys.error(s"$a / $b")
      } catch {
        case e: IllegalArgumentException => e.getMessage.contains(b.toString)
      }
    },
    "zero long" -> forAll { a: MemorySize =>
      try {
        a / 0L
        sys.error(s"$a / 0L")
      } catch {
        case _: ArithmeticException => true
      }
    },
    "positive double" -> forAllWith(Gen.positiveFiniteDouble) { (a, b) =>
      if (a.value / b <= Long.MaxValue) {
        a / b == MemorySize((a.value / b).toLong)
      } else try {
        a / b
        sys.error(s"$a / $b")
      } catch {
        case _: ArithmeticException => true
      }
    },
    "negative double" -> forAllWith(Gen.negativeFiniteDouble) { (a, b) =>
      if (a.value == 0L) {
        a / b == MemorySize.Zero
      } else try {
        a / b
        sys.error(s"$a / $b")
      } catch {
        case e: IllegalArgumentException => e.getMessage.contains(b.toString)
      }
    },
    "infinite double" -> forAllWith(infiniteDoubleGen) { (a, b) =>
      try {
        a / b
        sys.error(s"$a / $b")
      } catch {
        case e: IllegalArgumentException => e.getMessage.contains(b.toString)
      }
    },
    "NaN" -> forAll { a: MemorySize =>
      try {
        a / Double.NaN
        sys.error(s"$a / ${Double.NaN}")
      } catch {
        case e: IllegalArgumentException => e.getMessage.contains("NaN")
      }
    },
    "MemorySize" -> forAllWith(Gen[MemorySize]) { (a, b) =>
      a / b == a.value.toDouble / b.value.toDouble
    })

  val `<<` = Properties.properties("by")(
    "int" -> forAllWith(Gen[Int]) { (a, b) =>
      if (((a.value << b & 0x7fffffffffffffffL) >> b) == a.value) {
        (a << b) == MemorySize(a.value << b)
      } else try {
        a << b
        sys.error(s"$a << $b")
      } catch {
        case _: ArithmeticException => true
      }
    },
    "long" -> forAllWith(Gen[Long]) { (a, b) =>
      if (((a.value << b & 0x7fffffffffffffffL) >> b) == a.value) {
        (a << b) == MemorySize(a.value << b)
      } else try {
        a << b
        sys.error(s"$a << $b")
      } catch {
        case _: ArithmeticException => true
      }
    })

  val `>>` = Properties.properties("by")(
    "int" -> forAllWith(Gen[Int]) { (a, b) =>
      (a >> b) == MemorySize(a.value >> b)
    },
    "long" -> forAllWith(Gen[Long]) { (a, b) =>
      (a >> b) == MemorySize(a.value >> b)
    })

  val `>>>` = Properties.properties("by")(
    "int" -> forAllWith(Gen[Int]) { (a, b) =>
      (a >>> b) == MemorySize(a.value >>> b)
    },
    "long" -> forAllWith(Gen[Long]) { (a, b) =>
      (a >>> b) == MemorySize(a.value >>> b)
    })

  val asBytes = forAll { m: MemorySize =>
    m.asBytes == Bytes(m.value)
  }

}
