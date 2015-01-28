import sbt._

object Dependencies {
  val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

  val bonecp = "com.jolbox" % "bonecp" % "0.8.0.RELEASE"

  val scalikejdbc       = "org.scalikejdbc"     %% "scalikejdbc"        % "2.2.2"
  val scalikejdbcAsync  = "org.scalikejdbc"     %% "scalikejdbc-async"  % "0.5.5"
  val postgresqlAsync   = "com.github.mauricio" %% "postgresql-async"   % "0.2.15"
  val mysqlAsync        = "com.github.mauricio" %% "mysql-async"        % "0.2.15"

  val scalaTest   = "org.scalatest"   %% "scalatest"  % "2.2.3"
  val scalaCheck  = "org.scalacheck"  %% "scalacheck" % "1.12.1"
  val slf4j_nop   = "org.slf4j"        % "slf4j-nop"  % "1.7.10"

  val postgresql  = "org.postgresql"   % "postgresql"           % "9.3-1102-jdbc41"
  val mysql       = "mysql"            % "mysql-connector-java" % "5.1.34"
  val h2db        = "com.h2database"   % "h2"                   % "1.4.185"
  val hsqldb      = "org.hsqldb"       % "hsqldb"               % "2.3.2"
}
