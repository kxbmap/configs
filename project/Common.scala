import sbt.Keys._
import sbt._

object Common extends AutoPlugin {

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.11.8",
    crossScalaVersions += "2.12.0-RC1",
    description := "Scala wrapper for Typesafe config",
    organization := "com.github.kxbmap",
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

}
