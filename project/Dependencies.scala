import sbt._

object Dependencies {
  val typesafeConfig = "com.typesafe" % "config" % "1.0.2"

  val bonecp = "com.jolbox" % "bonecp" % "0.7.1.RELEASE"

  val scalikejdbc       = "com.github.seratch"  %% "scalikejdbc"        % "1.6.8"
  val scalikejdbcAsync  = "com.github.seratch"  %% "scalikejdbc-async"  % "0.2.5"
  val postgresqlAsync   = "com.github.mauricio" %% "postgresql-async"   % "0.2.7"
  val mysqlAsync        = "com.github.mauricio" %% "mysql-async"        % "0.2.7"

  val scalaTest   = "org.scalatest"   %% "scalatest"  % "2.0.RC1-SNAP4"
  val scalaCheck  = "org.scalacheck"  %% "scalacheck" % "1.10.1"
  val slf4j_nop   = "org.slf4j"        % "slf4j-nop"  % "1.7.5"

  val postgresql  = "postgresql"       % "postgresql"           % "9.1-901.jdbc4"
  val mysql       = "mysql"            % "mysql-connector-java" % "5.1.26"
  val h2db        = "com.h2database"   % "h2"                   % "1.3.173"
  val hsqldb      = "org.hsqldb"       % "hsqldb"               % "2.3.0"
}
