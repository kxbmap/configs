import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Dependencies extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    val configVersion = settingKey[String]("Typesafe config version")
    val scalaJava8CompatVersion = settingKey[String]("scala-java8-compat version")

    object dependencies {

      val scalaJava8Compat = Def.setting {
        "org.scala-lang.modules" %% "scala-java8-compat" % scalaJava8CompatVersion.value
      }

      val core = libraryDependencies ++= Seq(
        "com.typesafe" % "config" % configVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
        "org.projectlombok" % "lombok" % "1.16.8" % "test",
        scalaJava8Compat.value % "test"
      )
    }

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    configVersion := "1.3.0",
    scalapropsVersion := "0.3.0",
    scalaJava8CompatVersion := "0.7.0"
  )

}
