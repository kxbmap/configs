name := "configs"
description := "A Scala wrapper for Typesafe config"
organization := "com.github.kxbmap"

scalaVersion := "2.11.7"
crossScalaVersions += "2.12.0-M2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xexperimental",
  "-language:higherKinds",
  "-language:experimental.macros"
)

testFrameworks += new TestFramework("scalaprops.ScalapropsFramework")
parallelExecution in Test := false

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  "com.github.scalaprops" %% "scalaprops" % "0.1.11" % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion.value % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
)

lazy val scalaTestVersion = settingKey[String]("Version of ScalaTest")

scalaTestVersion := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => "2.2.5-M2"
    case _             => "2.2.5"
  }
}
