import sbt._

object Dependencies {
  val config      = "com.typesafe"     % "config"     % "1.0.2"
  val scalaTest   = "org.scalatest"   %% "scalatest"  % "2.0.RC1-SNAP4"
  val scalaCheck  = "org.scalacheck"  %% "scalacheck" % "1.10.1"
  val bonecp      = "com.jolbox"       % "bonecp"     % "0.7.1.RELEASE"
  val slf4j_nop   = "org.slf4j"        % "slf4j-nop"  % "1.7.5"
}
