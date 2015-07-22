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

import com.typesafe.config.{ConfigException, ConfigFactory}

//noinspection OptionEqualsSome,EmptyCheck
class OptionInstanceSpec extends UnitSpec {

  val config = ConfigFactory.parseString(
    """foo.v = 42
      |missing-v {}
      |""".stripMargin)

  case class Foo(v: Int)

  implicit val fooConfigs: Configs[Foo] = c => Foo(c.getInt("v"))


  describe("Configs[Option[T]]") {

    val C = Configs[Option[Foo]]

    it("extract Some[T]") {
      assert(C.extract(config.getConfig("foo")) == Some(Foo(42)))
    }
  }

  describe("AtPath[Option[T]]") {

    val A = AtPath[Option[Foo]]

    describe("extract a path to value function") {

      val f = A.extract(config)

      it("returns Some[T] if value exists") {
        assert(f("foo") == Some(Foo(42)))
      }

      it("returns None if path is missing") {
        assert(f("missing") == None)
      }

      it("throws an error if value type is wrong") {
        intercept[ConfigException.WrongType] {
          f("foo.v")
        }
      }
    }

    describe("behave same as Configs[Option[T]]") {

      val C = Configs[Option[Foo]]

      it("`getConfig and then Configs.extract` == `AtPath.extract and then apply`") {
        assert(C.extract(config.getConfig("foo")) == A.extract(config)("foo"))

        val e1 = intercept[ConfigException.Missing] {
          C.extract(config.getConfig("missing-v"))
        }
        val e2 = intercept[ConfigException.Missing] {
          A.extract(config)("missing-v")
        }
        assert(e1.getMessage == e2.getMessage)
      }
    }
  }

}
