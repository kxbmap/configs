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

import com.typesafe.config.{Config, ConfigFactory}
import scalikejdbc.async.AsyncConnectionPool

object AsyncDBs {

  private def setup(name: Symbol, dbs: AsyncDBSettings): Unit =
    AsyncConnectionPool.add(name, dbs.url, dbs.user, dbs.password, dbs.asyncPool)

  def setup(name: Symbol = AsyncConnectionPool.DEFAULT_NAME, path: String = "db")
           (implicit config: Config = ConfigFactory.load()): Unit =
    setup(name, config.get[AsyncDBSettings](s"$path.${name.name}"))

  def setupAll(path: String = "db")(implicit config: Config = ConfigFactory.load()): Unit =
    for ((name, dbs) <- config.get[Map[Symbol, AsyncDBSettings]](path)) {
      setup(name, dbs)
    }

  def close(name: Symbol = AsyncConnectionPool.DEFAULT_NAME): Unit =
    AsyncConnectionPool.close(name)

  def closeAll(): Unit =
    AsyncConnectionPool.closeAll()


//  def loadGlobalSettings(path: String = "scalikejdbc.global")(implicit config: Config = ConfigFactory.load()): Unit =
//    DBs.loadGlobalSettings(path)(config)
}
