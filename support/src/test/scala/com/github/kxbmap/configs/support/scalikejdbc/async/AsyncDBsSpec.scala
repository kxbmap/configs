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
package support.scalikejdbc.async

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}
import scalikejdbc.async.AsyncConnectionPool
import scalikejdbc.globalsettings.InfoLoggingForIgnoredParams
import scalikejdbc.{NameBindingSQLValidatorSettings, SQLFormatterSettings, LoggingSQLAndTimeSettings, GlobalSettings}

class AsyncDBsSpec extends FunSpec with Matchers with BeforeAndAfter {

  after {
    AsyncConnectionPool.closeAll()
  }

  describe("setup") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.default.url = "jdbc:mysql://localhost:3306/configs"
          |db.default.user = kxbmap
          |db.default.password = secret
          |""".stripMargin)

      AsyncDBs.setup()

      AsyncConnectionPool.get('default) should not be null
    }
  }

  describe("setupAll") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.db1.url = "jdbc:mysql://localhost:3306/configs"
          |db.db1.user = kxbmap
          |db.db1.password = secret
          |db.db2.url = "jdbc:postgresql://localhost/configs"
          |""".stripMargin)

      AsyncDBs.setupAll()

      AsyncConnectionPool.get('db1) should not be null
      AsyncConnectionPool.get('db2) should not be null
    }
  }

  describe("close") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.default.url = "jdbc:mysql://localhost:3306/configs"
          |db.default.user = kxbmap
          |db.default.password = secret
          |""".stripMargin)

      AsyncDBs.setup()
      AsyncDBs.close()

      AsyncConnectionPool.get() shouldBe null
    }
  }

  describe("closeAll") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.db1.url = "jdbc:mysql://localhost:3306/configs"
          |db.db1.user = kxbmap
          |db.db1.password = secret
          |db.db2.url = "jdbc:postgresql://localhost/configs"
          |""".stripMargin)

      AsyncDBs.setupAll()
      AsyncDBs.closeAll()

      AsyncConnectionPool.get('db1) shouldBe null
      AsyncConnectionPool.get('db2) shouldBe null
    }
  }


  describe("loadGlobalSettings") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """scalikejdbc.global {
          |  loggingSQLErrors = false
          |  loggingSQLAndTime {
          |    enabled = false
          |    singleLineMode = true
          |    logLevel = info
          |    warningEnabled = true
          |    warningThresholdMillis = 1000
          |    warningLogLevel = error
          |  }
          |  sqlFormatter.formatterClassName = com.example.SQLFormatter
          |  nameBindingSQLValidator.ignoredParams = infoLogging
          |}
          |""".stripMargin)

      AsyncDBs.loadGlobalSettings()

      GlobalSettings.loggingSQLErrors shouldBe false
      GlobalSettings.loggingSQLAndTime shouldBe LoggingSQLAndTimeSettings(
        enabled = false,
        singleLineMode = true,
        logLevel = 'info,
        warningEnabled = true,
        warningThresholdMillis = 1000L,
        warningLogLevel = 'error
      )
      GlobalSettings.sqlFormatter shouldBe SQLFormatterSettings(
        formatterClassName = "com.example.SQLFormatter"
      )
      GlobalSettings.nameBindingSQLValidator shouldBe NameBindingSQLValidatorSettings(
        ignoredParams = InfoLoggingForIgnoredParams
      )
    }
  }
}
