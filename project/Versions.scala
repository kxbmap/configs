import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Versions extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val configVersion = settingKey[String]("Typesafe config version")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.11.7",
    crossScalaVersions += "2.12.0-M2",
    configVersion := "1.3.0",
    scalapropsVersion := "0.1.13"
  )

}
