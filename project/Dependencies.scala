import sbt._

object Dependencies {
  val typesafeConfig = "com.typesafe" % "config" % "1.2.0"

  val bonecp = "com.jolbox" % "bonecp" % "0.8.0.RELEASE"

  val scalikejdbc       = "org.scalikejdbc"     %% "scalikejdbc"        % "1.7.4"
  val scalikejdbcAsync  = "org.scalikejdbc"     %% "scalikejdbc-async"  % "0.3.5"
  val postgresqlAsync   = "com.github.mauricio" %% "postgresql-async"   % "0.2.11"
  val mysqlAsync        = "com.github.mauricio" %% "mysql-async"        % "0.2.11"

  val scalaTest   = "org.scalatest"   %% "scalatest"  % "2.0"
  val scalaCheck  = "org.scalacheck"  %% "scalacheck" % "1.10.1"
  val slf4j_nop   = "org.slf4j"        % "slf4j-nop"  % "1.7.5"

  val postgresql  = "org.postgresql"   % "postgresql"           % "9.3-1100-jdbc41"
  val mysql       = "mysql"            % "mysql-connector-java" % "5.1.28"
  val h2db        = "com.h2database"   % "h2"                   % "1.3.174"
  val hsqldb      = "org.hsqldb"       % "hsqldb"               % "2.3.1"
}
