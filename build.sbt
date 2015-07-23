version in ThisBuild := "0.3.0-SNAPSHOT"

commonSettings

lazy val core = project.settings(
  commonSettings,
  name := "configs-core",
  description := "A Scala wrapper for Typesafe config",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.0"
  )
)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  organization := "com.github.kxbmap",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-Xexperimental",
    "-language:higherKinds"
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
  )
)
