/*
 * Copyright 2013-2015 Tsukasa Kitachi
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

package com.github.kxbmap.configs

import com.github.kxbmap.configs.util._
import java.{util => ju}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.string._


object BytesTest extends Scalaprops with ConfigProp {

  val bytes = check[Bytes]

  val bytesJList = {
    implicit val h = hideConfigs[Bytes]
    check[ju.List[Bytes]]
  }


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

  val multiply = {
    val bxd = forAll { (l: Bytes, r: Double) =>
      l * r == Bytes((l.value * r).toLong)
    }
    val dxb = forAll { (l: Double, r: Bytes) =>
      l * r == Bytes((l * r.value).toLong)
    }
    Properties.list(
      bxd.toProperties("Bytes * Double"),
      dxb.toProperties("Double * Bytes")
    )
  }

  val divide = {
    val bdd = forAll { (l: Bytes, r: Double) =>
      l / r == Bytes((l.value / r).toLong)
    }
    val bdb = forAll { (l: Bytes, r: Bytes) =>
      val result = l / r
      val expected = l.value.toDouble / r.value.toDouble
      if (result.isNaN) expected.isNaN else result == expected
    }
    Properties.list(
      bdd.toProperties("Bytes / Double"),
      bdb.toProperties("Bytes / Bytes")
    )
  }

  val unary_- = forAll { b: Bytes =>
    -b == Bytes(-b.value)
  }

  val unary_+ = forAll { b: Bytes =>
    +b == b
  }


  implicit lazy val bytesGen: Gen[Bytes] = Gen[Long].map(Bytes.apply)

  implicit lazy val bytesConfigVal: ConfigVal[Bytes] = ConfigVal[Long].contramap(_.value)

}
