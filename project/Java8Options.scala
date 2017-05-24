import Common.autoImport._
import sbt.Keys._
import sbt._

object Java8Options extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = Common

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => Seq(
          "-target:jvm-1.8",
          "-Ybackend:GenBCode",
          "-Ydelambdafy:method"
        )
        case _ => Nil
      }
    },
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => scalaJava8Compat :: Nil
        case _ => Nil
      }
    }
  )

}
