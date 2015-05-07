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

import com.typesafe.config.{ConfigFactory, Config}
import scalikejdbc.{NameBindingSQLValidatorSettings, SQLFormatterSettings, LoggingSQLAndTimeSettings, GlobalSettings, ConnectionPool}

object DBs {

  private def setup(name: Symbol, dbs: DBSettings): Unit = {
    Class.forName(dbs.driver)
    ConnectionPool.add(name, dbs.url, dbs.user, dbs.password, dbs.pool)
  }

  def setup(name: Symbol = ConnectionPool.DEFAULT_NAME, path: String = "db")
           (implicit config: Config = ConfigFactory.load()): Unit =
    setup(name, config.get[DBSettings](s"$path.${name.name}"))

  def setupAll(path: String = "db")(implicit config: Config = ConfigFactory.load()): Unit =
    for ((name, dbs) <- config.get[Map[Symbol, DBSettings]](path)) {
      setup(name, dbs)
    }

  def close(name: Symbol = ConnectionPool.DEFAULT_NAME): Unit =
    ConnectionPool.close(name)

  def closeAll(): Unit =
    ConnectionPool.closeAll()


  def loadGlobalSettings(path: String = "scalikejdbc.global")(implicit config: Config = ConfigFactory.load()): Unit =
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
