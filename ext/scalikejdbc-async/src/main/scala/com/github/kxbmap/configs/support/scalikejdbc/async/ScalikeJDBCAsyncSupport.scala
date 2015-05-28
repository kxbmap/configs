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

import scala.concurrent.duration.Duration
import scalikejdbc.async.AsyncConnectionPoolSettings

trait ScalikeJDBCAsyncSupport {

  implicit val asyncConnectionPoolSettingsConfigs: Configs[AsyncConnectionPoolSettings] = Configs.configs { c =>
    lazy val default = AsyncConnectionPoolSettings()
    AsyncConnectionPoolSettings(
      maxPoolSize = c.getOrElse("maxPoolSize", default.maxPoolSize),
      maxQueueSize = c.getOrElse("maxQueueSize", default.maxQueueSize),
      maxIdleMillis = c.opt[Long]("maxIdleMillis") getOrElse
        c.opt[Duration]("maxIdle").fold(default.maxIdleMillis)(_.toMillis)
    )
  }
}
