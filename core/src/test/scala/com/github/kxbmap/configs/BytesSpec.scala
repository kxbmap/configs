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

import com.typesafe.config.ConfigFactory
import org.scalacheck.Arbitrary


class BytesSpec extends UnitSpec {

  describe("Bytes") {
    it("should be available to get a value") {
      val config = ConfigFactory.parseString("value = 1024b")
      assert(config.get[Bytes]("value") == Bytes(1024L))
    }

    it("should be available to get values as list") {
      val config = ConfigFactory.parseString("values = [100b, 1MB]")
      assert(config.get[List[Bytes]]("values") === List(Bytes(100L), Bytes(1000000L)))
    }

    it("should be available to get values as vector") {
      val config = ConfigFactory.parseString("values = [100b, 1MB]")
      assert(config.get[Vector[Bytes]]("values") === Vector(Bytes(100L), Bytes(1000000L)))
    }

    it("should be a instance of Ordering") {
      forAll { (bs: List[Bytes]) =>
        assert(bs.sorted == bs.map(_.value).sorted.map(Bytes.apply))
      }
    }

    it("should be a instance of Ordered") {
      forAll { (l: Bytes, r: Bytes) =>
        assert((l compare r) == (l.value compare r.value))
      }
    }

    describe("operator '+'") {
      it("should be available") {
        assert(Bytes(2) + Bytes(3) == Bytes(5))
      }
    }

    describe("operator '-'") {
      it("should be available") {
        assert(Bytes(2) - Bytes(3) == Bytes(-1))
      }
    }

    describe("operator '*'") {
      it("should be available with the right hand number") {
        assert(Bytes(2) * 3 == Bytes(6))
      }
      it("should be available with the left hand number") {
        assert(2 * Bytes(3) == Bytes(6))
      }
    }

    describe("operator '/'") {
      it("should be available with the right hand number") {
        assert(Bytes(6) / 3 == Bytes(2))
      }
      it("should be available with the right hand Bytes") {
        assert(Bytes(6) / Bytes(3) == 2)
      }
    }

    describe("operator unary '-'") {
      it("should be available") {
        assert(-Bytes(2) == Bytes(-2))
      }
    }

    describe("operator unary '+'") {
      it("should be available") {
        assert(+Bytes(2) == Bytes(2))
      }
    }
  }

  implicit val BytesArb = Arbitrary {
    Arbitrary.arbitrary[Long].map(Bytes.apply)
  }
}
