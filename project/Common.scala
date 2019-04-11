import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Common extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {

    val typesafeConfig = "com.typesafe" % "config" % "1.3.3"

    val lombok = "org.projectlombok" % "lombok" % "1.18.6"

    val scalaJava8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"

    val scalaReflect = Def.setting {
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    }

    val scalaCollectionCompact = "org.scala-lang.modules" %% "scala-collection-compat" % "1.0.0"
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.12.8",
    crossScalaVersions := Seq("2.12.8", "2.11.12", "2.13.0-RC1"),
    scalapropsVersion := "0.6.0",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros"
    ),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, x)) if x >= 12 => Seq(
          "-opt:l:method"
        )
        case Some((2, 11)) => Seq(
          "-Xexperimental"  // lambda syntax for SAM types
        )
        case _ => Nil
      }
    },
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
