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
package ext.bonecp

import com.jolbox.bonecp.BoneCPConfig
import scala.concurrent.duration._

trait BoneCPConfigs {

  /**
   * Configs for `BoneCPConfig`
   */
  implicit val boneCPConfigConfigs: Configs[BoneCPConfig] = Configs { c =>
    // Load default and app specific config files first
    val cfg = new BoneCPConfig()

    def set[T: AtPath](p: String, ps: String*)(setter: T => Unit) {
      (c.missing[T](p) /: ps)(_ orElse c.missing[T](_)) foreach setter
    }

    def setDuration(p: String)(setter: (Long, TimeUnit) => Unit) {
      set[Duration](p)(d => setter(d.length, d.unit))
    }

    set[String]("jdbcUrl", "url")(cfg.setJdbcUrl)
    set[String]("username", "user")(cfg.setUsername)
    set[String]("password", "pass")(cfg.setPassword)

    cfg.setReleaseHelperThreads(c.missingOrElse("releaseHelperThreads", 0))

    set[String]("poolName")(cfg.setPoolName)
    set[Int]("minConnectionsPerPartition")(cfg.setMinConnectionsPerPartition)
    set[Int]("maxConnectionsPerPartition")(cfg.setMaxConnectionsPerPartition)
    set[Int]("acquireIncrement")(cfg.setAcquireIncrement)
    set[Int]("partitionCount")(cfg.setPartitionCount)

    setDuration("idleConnectionTestPeriod")(cfg.setIdleConnectionTestPeriod)
    setDuration("idleMaxAge")(cfg.setIdleMaxAge)

    set[String]("connectionTestStatement")(cfg.setConnectionTestStatement)
    set[Int]("statementsCacheSize")(cfg.setStatementsCacheSize)
    set[String]("connectionHookClassName")(cfg.setConnectionHookClassName)
    set[String]("initSQL")(cfg.setInitSQL)

    set[Boolean]("closeConnectionWatch")(cfg.setCloseConnectionWatch)
    set[Boolean]("logStatementsEnabled")(cfg.setLogStatementsEnabled)
    setDuration("acquireRetryDelay")(cfg.setAcquireRetryDelay)
    set[Boolean]("lazyInit")(cfg.setLazyInit)
    set[Boolean]("transactionRecoveryEnabled")(cfg.setTransactionRecoveryEnabled)
    set[Int]("acquireRetryAttempts")(cfg.setAcquireRetryAttempts)
    set[Boolean]("disableJMX")(cfg.setDisableJMX)

    setDuration("queryExecuteTimeLimit")(cfg.setQueryExecuteTimeLimit)
    set[Int]("poolAvailabilityThreshold")(cfg.setPoolAvailabilityThreshold)
    set[Boolean]("disableConnectionTracking")(cfg.setDisableConnectionTracking)
    setDuration("connectionTimeout")(cfg.setConnectionTimeout)
    setDuration("closeConnectionWatchTimeout")(cfg.setCloseConnectionWatchTimeout)
    set[Int]("statementReleaseHelperThreads")(cfg.setStatementReleaseHelperThreads)
    setDuration("maxConnectionAge")(cfg.setMaxConnectionAge)

    set[String]("configFile")(cfg.setConfigFile)
    set[String]("serviceOrder")(cfg.setServiceOrder)
    set[Boolean]("statisticsEnabled")(cfg.setStatisticsEnabled)
    set[Boolean]("defaultAutoCommit")(cfg.setDefaultAutoCommit(_))
    set[Boolean]("defaultReadOnly")(cfg.setDefaultReadOnly(_))
    set[String]("defaultCatalog")(cfg.setDefaultCatalog)
    set[String]("defaultTransactionIsolation")(cfg.setDefaultTransactionIsolation)
    set[Boolean]("externalAuth")(cfg.setExternalAuth)

    cfg
  }

}
