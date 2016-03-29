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
import configs.testutil.instance.string._
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}
import scalaz.syntax.std.boolean._

object EnrichConfigMemorySizeTest extends Scalaprops {

  val + = forAll { (a: ConfigMemorySize, b: ConfigMemorySize) =>
    (a.value + b.value >= 0L) --> {
      a + b == ConfigMemorySize(a.value + b.value)
    }
  }

  val - = forAll { (a: ConfigMemorySize, b: ConfigMemorySize) =>
    (a.value - b.value >= 0L) --> {
      a - b == ConfigMemorySize(a.value - b.value)
    }
  }

  val `*` = Properties.properties("by")(
    "int" -> forAll { (a: ConfigMemorySize, b: Int) =>
      (a.value * b >= 0L) --> {
        a * b == ConfigMemorySize(a.value * b)
      }
    },
    "long" -> forAll { (a: ConfigMemorySize, b: Long) =>
      (a.value * b >= 0L) --> {
        a * b == ConfigMemorySize(a.value * b)
      }
    },
    "double" -> forAll { (a: ConfigMemorySize, b: Double) =>
      (a.value * b >= 0L) --> {
        a * b == ConfigMemorySize((a.value * b).toLong)
      }
    })

  val `/` = Properties.properties("by")(
    "int" -> forAll { (a: ConfigMemorySize, b: Int) =>
      (b != 0L && a.value / b >= 0L) --> {
        a / b == ConfigMemorySize(a.value / b)
      }
    },
    "long" -> forAll { (a: ConfigMemorySize, b: Long) =>
      (b != 0L && a.value / b >= 0L) --> {
        a / b == ConfigMemorySize(a.value / b)
      }
    },
    "double" -> forAll { (a: ConfigMemorySize, b: Double) =>
      (a.value / b >= 0L) --> {
        a / b == ConfigMemorySize((a.value / b).toLong)
      }
    },
    "ConfigMemorySize" -> forAll { (a: ConfigMemorySize, b: ConfigMemorySize) =>
      (b != ConfigMemorySize.Zero && a.value / b.value >= 0L) --> {
        a / b == a.value.toDouble / b.value.toDouble
      }
    })

  val `<<` = Properties.properties("by")(
    "int" -> forAll { (a: ConfigMemorySize, b: Int) =>
      (a.value << b >= 0L) --> {
        (a << b) == ConfigMemorySize(a.value << b)
      }
    },
    "long" -> forAll { (a: ConfigMemorySize, b: Long) =>
      (a.value << b >= 0L) --> {
        (a << b) == ConfigMemorySize(a.value << b)
      }
    })

  val `>>` = Properties.properties("by")(
    "int" -> forAll { (a: ConfigMemorySize, b: Int) =>
      (a >> b) == ConfigMemorySize(a.value >> b)
    },
    "long" -> forAll { (a: ConfigMemorySize, b: Long) =>
      (a >> b) == ConfigMemorySize(a.value >> b)
    })

  val `>>>` = Properties.properties("by")(
    "int" -> forAll { (a: ConfigMemorySize, b: Int) =>
      (a >>> b) == ConfigMemorySize(a.value >>> b)
    },
    "long" -> forAll { (a: ConfigMemorySize, b: Long) =>
      (a >>> b) == ConfigMemorySize(a.value >>> b)
    })

}
