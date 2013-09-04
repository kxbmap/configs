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
import scalikejdbc.globalsettings.{ExceptionForIgnoredParams, WarnLoggingForIgnoredParams, InfoLoggingForIgnoredParams, IgnoredParamsValidation, NoCheckForIgnoredParams}
import scalikejdbc.{NameBindingSQLValidatorSettings, SQLFormatterSettings, LoggingSQLAndTimeSettings, ConnectionPoolSettings}

class ScalikeJDBCSupportSpec extends FunSpec with Matchers {

  describe("connectionPoolSettingsConfigs") {

    it ("should be available") {
      val c = ConfigFactory.empty()
      c.extract[ConnectionPoolSettings] shouldBe ConnectionPoolSettings()
    }

    it ("should be available with parameters") {
      val c = ConfigFactory.parseString(
        """initialSize = 10
          |maxSize = 42
          |connectionTimeoutMillis = 1000
          |validationQuery = "/* ping */ select 1;"
          |""".stripMargin)

      c.extract[ConnectionPoolSettings] shouldBe ConnectionPoolSettings(
        initialSize = 10,
        maxSize = 42,
        connectionTimeoutMillis = 1000L,
        validationQuery = "/* ping */ select 1;"
      )
    }

    it ("should be available with a duration parameter") {
      val c = ConfigFactory.parseString("connectionTimeout = 2s")
      c.extract[ConnectionPoolSettings] shouldBe ConnectionPoolSettings(
        connectionTimeoutMillis = 2000L
      )
    }
  }

  describe("loggingSQLAndTimeSettingsConfigs") {

    it ("should be available") {
      val c = ConfigFactory.empty()
      c.extract[LoggingSQLAndTimeSettings] shouldBe LoggingSQLAndTimeSettings()
    }

    it ("should be available with parameters") {
      val c = ConfigFactory.parseString(
        """enabled = false
          |singleLineMode = true
          |logLevel = info
          |warningEnabled = true
          |warningThresholdMillis = 1000
          |warningLogLevel = error
          |""".stripMargin)

      c.extract[LoggingSQLAndTimeSettings] shouldBe LoggingSQLAndTimeSettings(
        enabled = false,
        singleLineMode = true,
        logLevel = 'info,
        warningEnabled = true,
        warningThresholdMillis = 1000L,
        warningLogLevel = 'error
      )
    }

    it ("should be available with a duration parameter") {
      val c = ConfigFactory.parseString("warningThreshold = 2s")
      c.extract[LoggingSQLAndTimeSettings] shouldBe LoggingSQLAndTimeSettings(
        warningThresholdMillis = 2000L
      )
    }
  }

  describe("sqlFormatterSettingsConfigs") {

    it ("should be available") {
      val c = ConfigFactory.empty()
      c.extract[SQLFormatterSettings] shouldBe SQLFormatterSettings()
    }

    it ("should be available with parameters") {
      val c = ConfigFactory.parseString("formatterClassName = com.example.SQLFormatter")

      c.extract[SQLFormatterSettings] shouldBe SQLFormatterSettings(
        formatterClassName = "com.example.SQLFormatter"
      )
    }
  }

  describe("ignoredParamsValidationAtPath") {

    it ("should be available for `noCheck`") {
      val c = ConfigFactory.parseString("ignoredParams = noCheck")
      c.get[IgnoredParamsValidation]("ignoredParams") shouldBe NoCheckForIgnoredParams
    }

    it ("should be available for `infoLogging`") {
      val c = ConfigFactory.parseString("ignoredParams = infoLogging")
      c.get[IgnoredParamsValidation]("ignoredParams") shouldBe InfoLoggingForIgnoredParams
    }

    it ("should be available for `warnLogging`") {
      val c = ConfigFactory.parseString("ignoredParams = warnLogging")
      c.get[IgnoredParamsValidation]("ignoredParams") shouldBe WarnLoggingForIgnoredParams
    }

    it ("should be available for `exception`") {
      val c = ConfigFactory.parseString("ignoredParams = exception")
      c.get[IgnoredParamsValidation]("ignoredParams") shouldBe ExceptionForIgnoredParams
    }

    it ("should throw ConfigException.BadValue for unknown value") {
      val c = ConfigFactory.parseString("ignoredParams = Foo")
      val e = intercept[ConfigException.BadValue] {
        c.get[IgnoredParamsValidation]("ignoredParams")
      }
      e.getMessage should include ("Foo")
    }
  }

  describe("nameBindingSQLValidatorSettingsConfigs") {

    it ("should be available") {
      val c = ConfigFactory.empty()
      c.extract[NameBindingSQLValidatorSettings] shouldBe NameBindingSQLValidatorSettings()
    }

    it ("should be available with parameters") {
      val c = ConfigFactory.parseString("ignoredParams = noCheck")

      c.extract[NameBindingSQLValidatorSettings] shouldBe NameBindingSQLValidatorSettings(
        ignoredParams = NoCheckForIgnoredParams
      )
    }
  }
}
