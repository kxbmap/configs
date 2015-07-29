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

import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._


object BytesTest extends Scalaprops with ConfigProp {

  implicit val bytesGen: Gen[Bytes] =
    Gen[Long].map(Bytes.apply)

  implicit val bytesValue: CValue[Bytes] = _.value

  val nonZeroLongGen: Gen[Long] =
    Gen.oneOf(Gen.negativeLong, Gen.positiveLong)

  val nonZeroBytesGen: Gen[Bytes] =
    nonZeroLongGen.map(Bytes.apply)

  val nonZeroDoubleGen: Gen[Double] =
    Gen.genLongAll.map { n =>
      import java.lang.Double.{isInfinite, isNaN, longBitsToDouble}
      val x = longBitsToDouble(n)
      if (isNaN(x) || isInfinite(x)) Double.MinPositiveValue else x
    }


  val bytes = check[Bytes]

  val collection = Properties.list(
    check[List[Bytes]].toProperties("list"),
    check[Vector[Bytes]].toProperties("vector"),
    check[Stream[Bytes]].toProperties("stream"),
    check[Array[Bytes]].toProperties("array")
  )

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
    val bdd = forAllG(Gen[Bytes], nonZeroDoubleGen) { (l, r) =>
      l / r == Bytes((l.value / r).toLong)
    }
    val bdb = forAllG(Gen[Bytes], nonZeroBytesGen) { (l, r) =>
      l / r == l.value.toDouble / r.value.toDouble
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

}
