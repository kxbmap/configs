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

import java.util.Locale

import com.typesafe.config.ConfigException
import scalikejdbc.globalsettings.{ExceptionForIgnoredParams, IgnoredParamsValidation, InfoLoggingForIgnoredParams, NoCheckForIgnoredParams, WarnLoggingForIgnoredParams}
import scalikejdbc.{ConnectionPoolSettings, LoggingSQLAndTimeSettings, NameBindingSQLValidatorSettings, SQLFormatterSettings}

import scala.concurrent.duration.Duration


trait ScalikeJDBCSupport {

  implicit val connectionPoolSettingsConfigs: Configs[ConnectionPoolSettings] = Configs.configs { c =>
    lazy val default = ConnectionPoolSettings()
    ConnectionPoolSettings(
      initialSize = c.getOrElse("initialSize", default.initialSize),
      maxSize = c.getOrElse("maxSize", default.maxSize),
      connectionTimeoutMillis = c.opt[Long]("connectionTimeoutMillis") getOrElse
        c.opt[Duration]("connectionTimeout").fold(default.connectionTimeoutMillis)(_.toMillis),
      validationQuery = c.getOrElse("validationQuery", default.validationQuery),
      connectionPoolFactoryName = c.getOrElse("connectionPoolFactoryName", default.connectionPoolFactoryName)
    )
  }

  implicit val loggingSQLAndTimeSettingsConfigs: Configs[LoggingSQLAndTimeSettings] = Configs.configs { c =>
    lazy val default = LoggingSQLAndTimeSettings()
    LoggingSQLAndTimeSettings(
      enabled = c.getOrElse("enabled", default.enabled),
      singleLineMode = c.getOrElse("singleLineMode", default.singleLineMode),
      printUnprocessedStackTrace = c.getOrElse("printUnprocessedStackTrace", default.printUnprocessedStackTrace),
      stackTraceDepth = c.getOrElse("stackTraceDepth", default.stackTraceDepth),
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
    v.toLowerCase(Locale.ENGLISH) match {
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
