import sbt.Keys._
import sbt._

object Common extends AutoPlugin {

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.11.7",
    description := "Scala wrapper for Typesafe config",
    organization := "com.github.kxbmap",
    scalacOptions ++= Seq(
      "-target:jvm-1.7",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-Xexperimental",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros"
    ),
    javacOptions ++= Seq(
      "-source", "1.7",
      "-target", "1.7"
    ),
    updateOptions := updateOptions.value.withCachedResolution(true)
  )

}
