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

import scala.concurrent.duration.Duration
import scalikejdbc.globalsettings.{ExceptionForIgnoredParams, WarnLoggingForIgnoredParams, InfoLoggingForIgnoredParams, NoCheckForIgnoredParams, IgnoredParamsValidation}
import scalikejdbc.{GlobalSettings, NameBindingSQLValidatorSettings, SQLFormatterSettings, LoggingSQLAndTimeSettings, ConnectionPoolSettings}
import com.typesafe.config.{Config, ConfigException}


trait ScalikeJDBCSupport {

  implicit val connectionPoolSettingsConfigs: Configs[ConnectionPoolSettings] = Configs.configs { c =>
    lazy val default = ConnectionPoolSettings()
    ConnectionPoolSettings(
      initialSize = c.getOrElse("initialSize", default.initialSize),
      maxSize = c.getOrElse("maxSize", default.maxSize),
      connectionTimeoutMillis = c.opt[Long]("connectionTimeoutMillis") getOrElse
        c.opt[Duration]("connectionTimeout").fold(default.connectionTimeoutMillis)(_.toMillis),
      validationQuery = c.getOrElse("validationQuery", default.validationQuery)
    )
  }

  implicit val loggingSQLAndTimeSettingsConfigs: Configs[LoggingSQLAndTimeSettings] = Configs.configs { c =>
    lazy val default = LoggingSQLAndTimeSettings()
    LoggingSQLAndTimeSettings(
      enabled = c.getOrElse("enabled", default.enabled),
      singleLineMode = c.getOrElse("singleLineMode", default.singleLineMode),
      logLevel = c.getOrElse("logLevel", default.logLevel),
      warningEnabled = c.getOrElse("warningEnabled", default.warningEnabled),
      warningThresholdMillis = c.opt[Long]("warningThresholdMillis") getOrElse
        c.opt[Duration]("warningThreshold").fold(default.warningThresholdMillis)(_.toMillis),
      warningLogLevel = c.getOrElse("warningLogLevel", default.warningLogLevel)
    )
  }

  implicit val sqlFormatterSettingsConfigs: Configs[SQLFormatterSettings] = Configs.configs { c =>
    SQLFormatterSettings(
      formatterClassName = c.opt[String]("formatterClassName")
    )
  }

  implicit val ignoredParamsValidationAtPath: AtPath[IgnoredParamsValidation] = Configs.atPath { (c, p) =>
    val v = c.getString(p)
    v.toLowerCase match {
      case "nocheck"     => NoCheckForIgnoredParams
      case "infologging" => InfoLoggingForIgnoredParams
      case "warnlogging" => WarnLoggingForIgnoredParams
      case "exception"   => ExceptionForIgnoredParams
      case _             => throw new ConfigException.BadValue(c.origin(), p, s"unknown value: '$v'")
    }
  }

  implicit val nameBindingSQLValidatorSettingsConfigs: Configs[NameBindingSQLValidatorSettings] = Configs.configs { c =>
    lazy val default = NameBindingSQLValidatorSettings()
    NameBindingSQLValidatorSettings(
      ignoredParams = c.getOrElse("ignoredParams", default.ignoredParams)
    )
  }
}


object ScalikeJDBCSupport {

  def loadGlobalSettings(config: Config, path: String = "scalikejdbc.global"): Unit =
    config.opt[Config](path) foreach { global =>
      global.opt[Boolean]("loggingSQLErrors") foreach {
        GlobalSettings.loggingSQLErrors = _
      }
      global.opt[LoggingSQLAndTimeSettings]("loggingSQLAndTime") foreach {
        GlobalSettings.loggingSQLAndTime = _
      }
      global.opt[SQLFormatterSettings]("sqlFormatter") foreach {
        GlobalSettings.sqlFormatter = _
      }
      global.opt[NameBindingSQLValidatorSettings]("nameBindingSQLValidator") foreach {
        GlobalSettings.nameBindingSQLValidator = _
      }
    }
}