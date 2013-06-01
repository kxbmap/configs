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

    set[String]("jdbcUrl", "url")(cfg.setJdbcUrl(_))
    set[String]("username", "user")(cfg.setUsername(_))
    set[String]("password", "pass")(cfg.setPassword(_))

    cfg.setReleaseHelperThreads(c.missingOrElse("releaseHelperThreads", 0))

    set[String]("poolName")(cfg.setPoolName(_))
    set[Int]("minConnectionPerPartition")(cfg.setMinConnectionsPerPartition(_))
    set[Int]("maxConnectionPerPartition")(cfg.setMaxConnectionsPerPartition(_))
    set[Int]("acquireIncrement")(cfg.setAcquireIncrement(_))
    set[Int]("partitionCount")(cfg.setPartitionCount(_))

    setDuration("idleConnectionTestPeriod")(cfg.setIdleConnectionTestPeriod(_, _))
    setDuration("idleMaxAge")(cfg.setIdleMaxAge(_, _))

    set[String]("connectionTestStatement")(cfg.setConnectionTestStatement(_))
    set[Int]("statementsCacheSize")(cfg.setStatementsCacheSize(_))
    set[String]("connectionHook")(cfg.setConnectionHookClassName(_))
    set[String]("initSQL")(cfg.setInitSQL(_))

    set[Boolean]("closeConnectionWatch")(cfg.setCloseConnectionWatch(_))
    set[Boolean]("logStatementsEnabled")(cfg.setLogStatementsEnabled(_))
    setDuration("acquireRetryDelay")(cfg.setAcquireRetryDelay(_, _))
    set[Boolean]("lazyInit")(cfg.setLazyInit(_))
    set[Boolean]("transactionRecoveryEnabled")(cfg.setTransactionRecoveryEnabled(_))
    set[Int]("acquireRetryAttempts")(cfg.setAcquireRetryAttempts(_))
    set[Boolean]("disableJMX")(cfg.setDisableJMX(_))

    setDuration("queryExecuteTimeLimit")(cfg.setQueryExecuteTimeLimit(_, _))
    set[Int]("poolAvailabilityThreshold")(cfg.setPoolAvailabilityThreshold(_))
    set[Boolean]("disableConnectionTracking")(cfg.setDisableConnectionTracking(_))
    setDuration("connectionTimeout")(cfg.setConnectionTimeout(_, _))
    setDuration("closeConnectionWatchTimeout")(cfg.setCloseConnectionWatchTimeout(_, _))
    set[Int]("statementReleaseHelperThreads")(cfg.setStatementReleaseHelperThreads(_))
    setDuration("maxConnectionAge")(cfg.setMaxConnectionAge(_, _))

    set[String]("configFile")(cfg.setConfigFile(_))
    set[String]("serviceOrder")(cfg.setServiceOrder(_))
    set[Boolean]("statisticsEnabled")(cfg.setStatisticsEnabled(_))
    set[Boolean]("defaultAutoCommit")(cfg.setDefaultAutoCommit(_))
    set[Boolean]("defaultReadOnly")(cfg.setDefaultReadOnly(_))
    set[String]("defaultCatalog")(cfg.setDefaultCatalog(_))
    set[String]("defaultTransactionIsolation")(cfg.setDefaultTransactionIsolation(_))
    set[Boolean]("externalAuth")(cfg.setExternalAuth(_))

    cfg
  }

}
