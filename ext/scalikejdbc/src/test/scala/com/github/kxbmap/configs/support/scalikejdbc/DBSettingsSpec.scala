/*
 * Copyright 2013 Tsukasa Kitachi
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
package support.scalikejdbc

import com.typesafe.config.{ConfigException, ConfigFactory}
import org.scalatest.{Matchers, FunSpec}
import scalikejdbc.ConnectionPoolSettings

class DBSettingsSpec extends FunSpec with Matchers {

  describe("Configs instance") {

    it ("should be available") {
      val c = ConfigFactory.parseString(
        """driver = "com.mysql.jdbc.Driver"
          |url = "jdbc:mysql://localhost/configs"
          |user = kxbmap
          |password = secret
          |pool.maxSize = 30
          |""".stripMargin)

      c.extract[DBSettings] shouldBe DBSettings(
        driver = "com.mysql.jdbc.Driver",
        url = "jdbc:mysql://localhost/configs",
        user = "kxbmap",
        password = "secret",
        pool = ConnectionPoolSettings(maxSize = 30)
      )
    }

    describe("guess driver from url") {

      it ("should be available for mysql") {
        val c = ConfigFactory.parseString(
          """url = "jdbc:mysql://localhost/configs"""")

        c.extract[DBSettings].driver shouldBe "com.mysql.jdbc.Driver"
      }

      it ("should be available for postgresql") {
        val c = ConfigFactory.parseString(
          """url = "jdbc:postgresql://localhost/configs"""")

        c.extract[DBSettings].driver shouldBe "org.postgresql.Driver"
      }

      it ("should be available for h2") {
        val c = ConfigFactory.parseString(
          """url = "jdbc:h2:mem:"""")

        c.extract[DBSettings].driver shouldBe "org.h2.Driver"
      }

      it ("should be available for hsqldb") {
        val c = ConfigFactory.parseString(
          """url = "jdbc:hsqldb:mem:"""")

        c.extract[DBSettings].driver shouldBe "org.hsqldb.jdbcDriver"
      }

      it ("should be available for mysql on heroku") {
        val c = ConfigFactory.parseString(
          """url = "mysql://localhost/configs"""")

        c.extract[DBSettings].driver shouldBe "com.mysql.jdbc.Driver"
      }

      it ("should be available for postgresql on heroku") {
        val c = ConfigFactory.parseString(
          """url = "postgres://localhost/configs"""")

        c.extract[DBSettings].driver shouldBe "org.postgresql.Driver"
      }

      it ("should throw a ConfigException.BadValue if driver cannot guessed") {
        val c = ConfigFactory.parseString(
          """url = "jdbc:some_unknown://localhost/configs"""")

        intercept[ConfigException.BadValue] {
          c.extract[DBSettings]
        }
      }
    }
  }
}
