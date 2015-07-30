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

scalapropsSettings
scalapropsVersion := "0.1.11"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  "org.scalatest" %% "scalatest" % scalaTestVersion.value % "test"
)

lazy val scalaTestVersion = settingKey[String]("Version of ScalaTest")

scalaTestVersion := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => "2.2.5-M2"
    case _             => "2.2.5"
  }
}
