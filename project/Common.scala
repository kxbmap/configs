import BuildUtil._
import sbt.Keys._
import sbt._

object Common extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.12.2", "2.11.11", "2.13.0-M1"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros"
    ),
    scalacOptions ++= byScalaVersion {
      case (2, x) if x >= 12 => Seq(
        "-opt:l:method"
      )
      case (2, 11) => Seq(
        // lambda syntax for SAM types
        "-Xexperimental"
      )
    }.value,
    updateOptions := updateOptions.value.withCachedResolution(true),
    incOptions := incOptions.value.withLogRecompileOnMacro(false)
  ) ++
    Seq(Compile, Test).map { c =>
      scalacOptions in (c, console) := {
        val opts = (scalacOptions in (c, console)).value
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, x)) if x >= 12 =>
            opts.map {
              case "-Xlint" => "-Xlint:-unused,_"
              case otherwise => otherwise
            }
          case _ => opts
        }
      }
    }

}
