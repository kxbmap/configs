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

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}
import scalikejdbc.globalsettings.InfoLoggingForIgnoredParams
import scalikejdbc.{ConnectionPool, NameBindingSQLValidatorSettings, SQLFormatterSettings, LoggingSQLAndTimeSettings, GlobalSettings}

class DBsSpec extends FunSpec with Matchers with BeforeAndAfter {

  after {
    ConnectionPool.closeAll()
  }

  describe("setup") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.default.url = "jdbc:mysql://localhost:3306/configs"
          |db.default.user = kxbmap
          |db.default.password = secret
          |""".stripMargin)

      DBs.setup()

      ConnectionPool.get() should not be null
    }
  }

  describe("setupAll") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.db1.url = "jdbc:mysql://localhost:3306/configs"
          |db.db1.user = kxbmap
          |db.db1.password = secret
          |db.db2.url = "jdbc:h2:mem:configs"
          |""".stripMargin)

      DBs.setupAll()

      ConnectionPool.get('db1) should not be null
      ConnectionPool.get('db2) should not be null
    }
  }

  describe("close") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.default.url = "jdbc:mysql://localhost:3306/configs"
          |db.default.user = kxbmap
          |db.default.password = secret
          |""".stripMargin)

      DBs.setup()
      DBs.close()

      a [IllegalStateException] should be thrownBy ConnectionPool.get()
    }
  }

  describe("closeAll") {

    it ("should be available") {
      implicit val c = ConfigFactory.parseString(
        """db.db1.url = "jdbc:mysql://localhost:3306/configs"
          |db.db1.user = kxbmap
          |db.db1.password = secret
          |db.db2.url = "jdbc:h2:mem:configs"
          |""".stripMargin)

      DBs.setupAll()
      DBs.closeAll()

      a [IllegalStateException] should be thrownBy ConnectionPool.get('db1)
      a [IllegalStateException] should be thrownBy ConnectionPool.get('db2)
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
          |    printUnprocessedStackTrace = true
          |    stackTraceDepth = 1
          |    logLevel = info
          |    warningEnabled = true
          |    warningThresholdMillis = 1000
          |    warningLogLevel = error
          |  }
          |  sqlFormatter.formatterClassName = com.example.SQLFormatter
          |  nameBindingSQLValidator.ignoredParams = infoLogging
          |}
          |""".stripMargin)

      DBs.loadGlobalSettings()

      GlobalSettings.loggingSQLErrors shouldBe false
      GlobalSettings.loggingSQLAndTime shouldBe LoggingSQLAndTimeSettings(
        enabled = false,
        singleLineMode = true,
        printUnprocessedStackTrace = true,
        stackTraceDepth = 1,
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
