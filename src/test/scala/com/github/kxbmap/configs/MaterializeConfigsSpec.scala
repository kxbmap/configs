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

class MaterializeConfigsSpec extends UnitSpec {

  import MaterializeConfigsSpec._

  describe("Configs auto materialization") {

    it("should provide for simple class") {
      val config = ConfigFactory.parseString(
        """
        user = Alice
        password = secret
        """)

      val s = config.extract[SimpleSetting]

      assert(s == SimpleSetting("Alice", "secret"))
    }

    it("should provide for nested class") {
      val config = ConfigFactory.parseString(
        """
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
      val config = ConfigFactory.parseString(
        """
        u = Alice
        p = secret
        """)

      val s = config.extract[SpecifiedSetting]

      assert(s == SpecifiedSetting("Alice", "secret"))
    }

    it("should provide for class that has multi parameter list") {
      val config = ConfigFactory.parseString(
        """
        firstName = John
        lastName = Doe
        age = 42
        """)

      val s = config.extract[HasParamLists]

      assert(s.firstName == "John")
      assert(s.lastName == "Doe")
      assert(s.age == 42)
    }


    describe("for class that has sub constructors") {

      it("should extract by primary constructor") {
        val config = ConfigFactory.parseString(
          """
          name = John Doe
          age = 42
          country = USA
          """)

        val s = config.extract[HasSubConstructors]

        assert(s == HasSubConstructors("John Doe", 42, "USA"))
      }

      it("should extract by most specific sub constructor") {
        val config = ConfigFactory.parseString(
          """
          firstName = John
          lastName = Doe
          age = 42
          """)

        val s = config.extract[HasSubConstructors]

        assert(s == HasSubConstructors("John Doe", 42, "JPN"))
      }

      it("should extract by other sub constructor") {
        val config = ConfigFactory.parseString(
          """
          firstName = John
          lastName = Doe
          """)

        val s = config.extract[HasSubConstructors]

        assert(s == HasSubConstructors("John Doe", 0, "JPN"))
      }

      it("should not extract by private constructor") {
        val config = ConfigFactory.parseString(
          """
          name = John Doe
          age = 42
          """)

        intercept[ConfigException.Missing] {
          config.extract[HasSubConstructors]
        }
      }

      it("should use primary constructor first") {
        val config = ConfigFactory.parseString(
          """
          firstName = John
          lastName = Doe
          name = Alice
          age = 10
          country = USA
          """)

        val s = config.extract[HasSubConstructors]

        assert(s == HasSubConstructors("Alice", 10, "USA"))
      }

    }


    describe("format key to lower-hyphen-case") {

      it("should extract config") {
        val config = ConfigFactory.parseString(
          """
          lower-camel-case = 0
          upper-camel-case = 1
          lower-snake-case = 2
          upper-snake-case = 3
          lower-hyphen-case = 4
          upper-then-camel = 5
          """)

        val s = config.extract[FormatCase]

        assert(s == FormatCase(0, 1, 2, 3, 4, 5))
      }

      it("should prevail original key") {
        val config = ConfigFactory.parseString(
          """
          lower-camel-case = 0
          upper-camel-case = 1
          lower-snake-case = 2
          upper-snake-case = 3
          upper-then-camel = 5
          lowerCamelCase = 42
          UpperCamelCase = 42
          lower_snake_case = 42
          UPPER_SNAKE_CASE = 42
          lower-hyphen-case = 42
          UPPERThenCamel = 42
          """)

        val s = config.extract[FormatCase]

        assert(s == FormatCase(42, 42, 42, 42, 42, 42))
      }

      it("should not use formatted key if duplicates") {
        val config = ConfigFactory.parseString(
          """
          duplicate-name = 0
          """)

        val e = intercept[ConfigException.Missing] {
          config.extract[Duplicate1]
        }
        assert(e.getMessage.contains("duplicateName"))
      }

      it("should not use formatted key if duplicate with other parameter name") {
        val config = ConfigFactory.parseString(
          """
          duplicate-name = 0
          """)

        val e = intercept[ConfigException.Missing] {
          config.extract[Duplicate2]
        }
        assert(e.getMessage.contains("duplicateName"))
      }

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


  case class HasSubConstructors(name: String, age: Int, country: String) {

    private def this(name: String, age: Int) = this(name, age, "JPN")

    def this(firstName: String, lastName: String) = this(s"$firstName $lastName", 0)

    def this(firstName: String, lastName: String, age: Int) = this(s"$firstName $lastName", age)
  }


  case class FormatCase(
    lowerCamelCase: Int,
    UpperCamelCase: Int,
    lower_snake_case: Int,
    UPPER_SNAKE_CASE: Int,
    `lower-hyphen-case`: Int,
    UPPERThenCamel: Int)


  case class Duplicate1(duplicateName: Int, DuplicateName: Int)

  case class Duplicate2(duplicateName: Int, `duplicate-name`: Int)

}
