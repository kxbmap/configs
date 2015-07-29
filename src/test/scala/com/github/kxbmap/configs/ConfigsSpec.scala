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
import org.scalatest.FunSpec

class ConfigsSpec extends FunSpec {

  import ConfigFactory.parseString

  describe("Configs") {

    it("should be a instance of Functor") {
      val c = parseString("value = 42")
      val cs: Configs[Int] = Configs.onPath(_.getInt("value"))

      assert(cs.map(identity).extract(c) == identity(cs).extract(c))

      val f = (_: Int) * 2
      val g = (_: Int).toLong
      assert(cs.map(g compose f).extract(c) == cs.map(f).map(g).extract(c))
    }


    describe("orElse") {

      val c1: Configs[Int] = Configs.onPath(_.getInt("value1"))
      val c2: Configs[Int] = Configs.onPath(_.getInt("value2"))
      val error: Configs[Int] = Configs.onPath[Int](_ => sys.error("error"))

      it("should extract a first value") {
        val config = parseString("""
          value1 = 42
          value2 = 0
          """)
        val n = c1.orElse(c2).extract(config)
        assert(n == 42)
      }

      it("should extract a second value if first one is failed") {
        val config = parseString("""
          value1 = not int
          value2 = 0
          """)
        val n = c1.orElse(c2).extract(config)
        assert(n == 0)
      }

      it("should throw an error if both first and second are failed") {
        val config = parseString("""
          value1 = not int
          """)
        intercept[ConfigException.Missing] {
          c1.orElse(c2).extract(config)
        }
      }

      it("should throw immediately if occurred an error other than ConfigException") {
        val config = parseString("""
          value1 = 42
          value2 = 0
          """)
        val e = intercept[RuntimeException] {
          error.orElse(c2).extract(config)
        }
        assert(e.getMessage == "error")
      }

    }

  }

}
