import sbt._

object Dependencies {
  val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

  val bonecp = "com.jolbox" % "bonecp" % "0.8.0.RELEASE"

  val scalikejdbc       = "org.scalikejdbc"     %% "scalikejdbc"        % "2.0.1"
  val scalikejdbcAsync  = "org.scalikejdbc"     %% "scalikejdbc-async"  % "0.4.0"
  val postgresqlAsync   = "com.github.mauricio" %% "postgresql-async"   % "0.2.13"
  val mysqlAsync        = "com.github.mauricio" %% "mysql-async"        % "0.2.13"

  val scalaTest   = "org.scalatest"   %% "scalatest"  % "2.1.7"
  val scalaCheck  = "org.scalacheck"  %% "scalacheck" % "1.11.4"
  val slf4j_nop   = "org.slf4j"        % "slf4j-nop"  % "1.7.7"

  val postgresql  = "org.postgresql"   % "postgresql"           % "9.3-1101-jdbc41"
  val mysql       = "mysql"            % "mysql-connector-java" % "5.1.30"
  val h2db        = "com.h2database"   % "h2"                   % "1.4.178"
  val hsqldb      = "org.hsqldb"       % "hsqldb"               % "2.3.2"
}
