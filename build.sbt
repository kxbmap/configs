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
  "-language:implicitConversions",
  "-language:experimental.macros"
)

scalapropsSettings
scalapropsVersion := "0.1.12"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
)
