import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Common extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {

    val typesafeConfig = "com.typesafe" % "config" % "1.4.1"

    val lombok = "org.projectlombok" % "lombok" % "1.18.10"

    val commonsText = "org.apache.commons" % "commons-text" % "1.9"

    val scalaJava8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"

    val scalaReflect = Def.setting {
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    }

    val scalaCollectionCompact = "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2"
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.13.1",
    crossScalaVersions := Seq("2.13.1", "2.12.10", "2.11.12"),
    scalapropsVersion := "0.6.1",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros"
      //"-Ymacro-debug-lite" // enable to see generated macro code
    ),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, x)) if x >= 12 => Seq("-opt:l:method")
        case _ => Nil
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => Seq("-Xexperimental") // lambda syntax for SAM types
        case _ => Nil
      }
    },
    updateOptions := updateOptions.value.withCachedResolution(true),
    incOptions := incOptions.value.withLogRecompileOnMacro(false)
  ) ++
    Seq(Compile, Test).map { c =>
      c / console / scalacOptions := suppressUnusedWarning((c / console / scalacOptions).value, scalaVersion.value)
    }

  def suppressUnusedWarning(scalacOptions: Seq[String], scalaVersion: String): Seq[String] =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, x)) if x >= 12 =>
        scalacOptions.map {
          case "-Xlint" => "-Xlint:-unused,_"
          case otherwise => otherwise
        }
      case _ => scalacOptions
    }

}
