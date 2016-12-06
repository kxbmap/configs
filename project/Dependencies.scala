import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Dependencies extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = Common

  object autoImport {
    val configVersion = settingKey[String]("Typesafe config version")
    val lombokVersion = settingKey[String]("lombok version")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    configVersion := "1.3.1",
    lombokVersion := "1.16.12",
    scalapropsVersion := "0.3.4"
  )

  lazy val core =
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % configVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
      "org.projectlombok" % "lombok" % lombokVersion.value % "test"
    )

  lazy val testutil =
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % configVersion.value,
      "com.github.scalaprops" %% "scalaprops" % scalapropsVersion.value,
      "org.projectlombok" % "lombok" % lombokVersion.value
    )

  lazy val docs =
    libraryDependencies ++= Seq(
      "org.projectlombok" % "lombok" % lombokVersion.value % "test"
    )

}
