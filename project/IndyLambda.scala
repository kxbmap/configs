import sbt.Keys._
import sbt._

object IndyLambda extends AutoPlugin {

  override def trigger = allRequirements

  override def requires: Plugins = Dependencies

  import Dependencies.autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= seq(scalaVersion.value)(
      "-target:jvm-1.8",
      "-Ybackend:GenBCode",
      "-Ydelambdafy:method"
    ),
    libraryDependencies ++= seq(scalaVersion.value)(
      dependencies.java8Compat.value
    )
  )

  private def seq[A](v: String)(xs: A*): Seq[A] =
    VersionNumber(v).numbers match {
      case Seq(2, 11, x, _*) if x >= 8 => xs
      case _ => Seq.empty
    }

}
