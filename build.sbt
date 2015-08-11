name := "configs"

commonSettings

lazy val configVersion = settingKey[String]("Typesafe config version")

lazy val commonSettings: Seq[Setting[_]] = Seq(
  description := "A Scala wrapper for Typesafe config",
  organization := "com.github.kxbmap",
  scalaVersion := "2.11.7",
  crossScalaVersions += "2.12.0-M2",
  configVersion := "1.3.0",
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
)

lazy val core = project.settings(
  name := "configs",
  commonSettings,
  scalapropsSettings,
  scalapropsVersion := "0.1.12",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % configVersion.value,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
  )
)
