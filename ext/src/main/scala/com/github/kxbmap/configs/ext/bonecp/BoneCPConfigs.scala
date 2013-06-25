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

    import Catcher.Implicits.missing

    def duration(p: String)(setter: (Long, TimeUnit) => Unit) {
      c.opt[Duration](p) foreach { d => setter(d.length, d.unit) }
    }

    c.opt[String]("jdbcUrl") orElse c.opt[String]("url") foreach cfg.setJdbcUrl
    c.opt[String]("username") orElse c.opt[String]("user") foreach cfg.setUsername
    c.opt[String]("password") orElse c.opt[String]("pass") foreach cfg.setPassword

    cfg.setReleaseHelperThreads(c.getOrElse("releaseHelperThreads", 0))

    c.opt[String]("poolName") foreach cfg.setPoolName
    c.opt[Int]("minConnectionsPerPartition") foreach cfg.setMinConnectionsPerPartition
    c.opt[Int]("maxConnectionsPerPartition") foreach cfg.setMaxConnectionsPerPartition
    c.opt[Int]("acquireIncrement") foreach cfg.setAcquireIncrement
    c.opt[Int]("partitionCount") foreach cfg.setPartitionCount

    duration("idleConnectionTestPeriod")(cfg.setIdleConnectionTestPeriod)
    duration("idleMaxAge")(cfg.setIdleMaxAge)

    c.opt[String]("connectionTestStatement") foreach cfg.setConnectionTestStatement
    c.opt[Int]("statementsCacheSize") foreach cfg.setStatementsCacheSize
    c.opt[String]("connectionHookClassName") foreach cfg.setConnectionHookClassName
    c.opt[String]("initSQL") foreach cfg.setInitSQL

    c.opt[Boolean]("closeConnectionWatch") foreach cfg.setCloseConnectionWatch
    c.opt[Boolean]("logStatementsEnabled") foreach cfg.setLogStatementsEnabled
    duration("acquireRetryDelay")(cfg.setAcquireRetryDelay)
    c.opt[Boolean]("lazyInit") foreach cfg.setLazyInit
    c.opt[Boolean]("transactionRecoveryEnabled") foreach cfg.setTransactionRecoveryEnabled
    c.opt[Int]("acquireRetryAttempts") foreach cfg.setAcquireRetryAttempts
    c.opt[Boolean]("disableJMX") foreach cfg.setDisableJMX

    duration("queryExecuteTimeLimit")(cfg.setQueryExecuteTimeLimit)
    c.opt[Int]("poolAvailabilityThreshold") foreach cfg.setPoolAvailabilityThreshold
    c.opt[Boolean]("disableConnectionTracking") foreach cfg.setDisableConnectionTracking
    duration("connectionTimeout")(cfg.setConnectionTimeout)
    duration("closeConnectionWatchTimeout")(cfg.setCloseConnectionWatchTimeout)
    c.opt[Int]("statementReleaseHelperThreads") foreach cfg.setStatementReleaseHelperThreads
    duration("maxConnectionAge")(cfg.setMaxConnectionAge)

    c.opt[String]("configFile") foreach cfg.setConfigFile
    c.opt[String]("serviceOrder") foreach cfg.setServiceOrder
    c.opt[Boolean]("statisticsEnabled") foreach cfg.setStatisticsEnabled
    c.opt[Boolean]("defaultAutoCommit") foreach { cfg.setDefaultAutoCommit(_) }
    c.opt[Boolean]("defaultReadOnly") foreach { cfg.setDefaultReadOnly(_) }
    c.opt[String]("defaultCatalog") foreach cfg.setDefaultCatalog
    c.opt[String]("defaultTransactionIsolation") foreach cfg.setDefaultTransactionIsolation
    c.opt[Boolean]("externalAuth") foreach cfg.setExternalAuth

    cfg
  }

}
