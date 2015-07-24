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

class MaterializeConfigsSpec extends UnitSpec {

  import MaterializeConfigsSpec._

  describe("Configs auto materialization") {

    it("should provide for simple class") {
      val config = ConfigFactory.parseString("""
        user = Alice
        password = secret
        """)

      val s = config.extract[SimpleSetting]

      assert(s == SimpleSetting("Alice", "secret"))
    }

    it("should provide for nested class") {
      val config = ConfigFactory.parseString("""
        simple = {
          user = Alice
          password = secret
        }
        simples = [
          { user = Bob, password = foo },
          { user = Charlie, password = bar }
        ]
        simpleMap = {
          dave = {
            user = Dave
            password = baz
          }
        }
        optional1 = {
          user = Ellen
          password = foobar
        }
        """)

      val s = config.extract[NestedSetting]

      assert(s == NestedSetting(
        SimpleSetting("Alice", "secret"),
        Seq(
          SimpleSetting("Bob", "foo"),
          SimpleSetting("Charlie", "bar")
        ),
        Map(
          "dave" -> SimpleSetting("Dave", "baz")
        ),
        Some(SimpleSetting("Ellen", "foobar")),
        None
      ))
    }

    it("should prevail the specified instance") {
      val config = ConfigFactory.parseString("""
        u = Alice
        p = secret
        """)

      val s = config.extract[SpecifiedSetting]

      assert(s == SpecifiedSetting("Alice", "secret"))
    }

    it("should provide for class that has multi parameter list") {
      val config = ConfigFactory.parseString("""
        firstName = John
        lastName = Doe
        age = 42
        """)

      val s = config.extract[HasParamLists]

      assert(s.firstName == "John")
      assert(s.lastName == "Doe")
      assert(s.age == 42)
    }

  }

}

object MaterializeConfigsSpec {

  case class SimpleSetting(user: String, password: String)

  case class NestedSetting(
    simple: SimpleSetting,
    simples: Seq[SimpleSetting],
    simpleMap: Map[String, SimpleSetting],
    optional1: Option[SimpleSetting],
    optional2: Option[SimpleSetting])


  case class SpecifiedSetting(user: String, password: String)

  object SpecifiedSetting {
    implicit val configs: Configs[SpecifiedSetting] = c => SpecifiedSetting(
      user = c.get[String]("u"),
      password = c.get[String]("p")
    )
  }


  class HasParamLists(val firstName: String, val lastName: String)(val age: Int)

}
