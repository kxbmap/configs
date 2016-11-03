import BuildUtil.scala211Only
import sbt.Keys._
import sbt._

object Java8Options extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = Common

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= scala211Only(
      "-target:jvm-1.8",
      "-Ybackend:GenBCode",
      "-Ydelambdafy:method"
    ).value,
    libraryDependencies ++= scala211Only(
      "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"
    ).value
  )

}
