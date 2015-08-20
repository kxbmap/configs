import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.pgp.PgpKeys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

object Release extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = ReleasePlugin && SbtPgp

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    releaseCrossBuild := true,
    releasePublishArtifactsAction := publishSigned.value,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion
    )
  )

}
