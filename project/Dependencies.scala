import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Dependencies extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    val configVersion = settingKey[String]("Typesafe config version")
    val lombokVersion = settingKey[String]("lombok version")

    object dependencies {

      val core = libraryDependencies ++= Seq(
        "com.typesafe" % "config" % configVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
        "org.projectlombok" % "lombok" % lombokVersion.value % "test"
      )

      val doc =
        libraryDependencies ++= Seq(
          "org.projectlombok" % "lombok" % lombokVersion.value % "test"
        )
    }

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    configVersion := "1.3.1",
    lombokVersion := "1.16.10",
    scalapropsVersion := "0.3.4"
  )

}
