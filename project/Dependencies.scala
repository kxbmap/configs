import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Dependencies extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    val configVersion = settingKey[String]("Typesafe config version")

    object dependencies {

      val core = libraryDependencies ++= Seq(
        "com.typesafe" % "config" % configVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
      )
    }

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    configVersion := "1.3.0",
    scalapropsVersion := "0.2.0"
  )

}
