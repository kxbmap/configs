import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Dependencies extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    val configVersion = settingKey[String]("Typesafe config version")
    val lombokVersion = settingKey[String]("lombok version")
    val scalaJava8CompatVersion = settingKey[String]("scala-java8-compat version")

    object dependencies {

      val scalaJava8Compat = Def.setting {
        "org.scala-lang.modules" %% "scala-java8-compat" % scalaJava8CompatVersion.value
      }

      val core = libraryDependencies ++= Seq(
        "com.typesafe" % "config" % configVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
        "org.projectlombok" % "lombok" % lombokVersion.value % "test",
        scalaJava8Compat.value % "test"
      )

      val doc =
        libraryDependencies ++= Seq(
          "org.projectlombok" % "lombok" % lombokVersion.value % "test"
        )
    }

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    configVersion := "1.3.0",
    lombokVersion := "1.16.10",
    scalapropsVersion := "0.3.2",
    scalaJava8CompatVersion := (scalaVersion.value match {
      case "2.12.0-M4" => "0.8.0-RC1"
      case _ => "0.7.0"
    })
  )

}
