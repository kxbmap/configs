name := "configs"

commonSettings
disablePublishSettings

lazy val configVersion = settingKey[String]("Typesafe config version")

lazy val commonSettings: Seq[Setting[_]] = Seq(
  description := "A Scala wrapper for Typesafe config",
  organization := "com.github.kxbmap",
  scalaVersion := "2.11.7",
  crossScalaVersions += "2.12.0-M2",
  configVersion := "1.3.0",
  scalapropsVersion := "0.1.13",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-Xexperimental",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros"
  ),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

lazy val core = project.settings(
  name := "configs",
  commonSettings,
  scalapropsSettings,
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % configVersion.value
  ),
  initialCommands :=
    """import com.typesafe.config._
      |import ConfigFactory._
      |import com.github.kxbmap.configs.{Configs, Bytes}
      |import com.github.kxbmap.configs.simple._
      |import com.github.kxbmap.configs.syntax._
      |""".stripMargin
).dependsOn(
  macros % "provided"
)

lazy val macros = project.settings(
  name := "configs-macro",
  commonSettings,
  scalapropsSettings,
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % configVersion.value,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
)
