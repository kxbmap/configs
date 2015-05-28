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

import com.typesafe.config.ConfigException
import scalikejdbc.ConnectionPoolSettings

case class DBSettings(driver: String,
                      url: String,
                      user: String,
                      password: String,
                      pool: ConnectionPoolSettings)

object DBSettings {

  implicit val dbSettingsConfigs: Configs[DBSettings] = Configs.configs { c =>
    val url = c.getString("url")
    DBSettings(
      driver = c.opt[String]("driver") orElse
        guessDriverFromUrl(url) getOrElse (
        throw new ConfigException.BadValue(c.origin(), "driver", s"Missing driver and cannot guess from url: $url")),
      url = url,
      user = c.getOrElse[String]("user", null),
      password = c.getOrElse[String]("password", null),
      pool = c.getOrElse("pool", ConnectionPoolSettings())
    )
  }

  private def guessDriverFromUrl(url: String): Option[String] =
    supportedDrivers collectFirst {
      case (p, d) if url.startsWith(p) => d
    }

  private def supportedDrivers = Map(
    "jdbc:mysql:" -> "com.mysql.jdbc.Driver",
    "mysql:" -> "com.mysql.jdbc.Driver",
    "jdbc:postgresql:" -> "org.postgresql.Driver",
    "postgres:" -> "org.postgresql.Driver",
    "jdbc:h2:" -> "org.h2.Driver",
    "jdbc:hsqldb:" -> "org.hsqldb.jdbcDriver"
  )
}
