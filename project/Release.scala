import com.jsuereth.sbtpgp.PgpKeys._
import com.jsuereth.sbtpgp.SbtPgp
import mdoc.MdocPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version.Bump

object Release extends AutoPlugin {

  override def requires: Plugins = Common && ReleasePlugin && SbtPgp

  object autoImport {
    val readmeFileName = settingKey[String]("Readme file name")
    val updateReadme = taskKey[File]("Update readme file")
  }

  import autoImport._

  private val docs = LocalProject("docs")

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    readmeFileName := "README.md",
    updateReadme := updateReadmeTask.value,
    releaseCrossBuild := true,
    releasePublishArtifactsAction := publishSigned.value,
    releaseVersionBump := Bump.Bugfix,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      releaseStepTask(updateReadme),
      commitReadme,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommand("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion
    )
  )

  private def updateReadmeTask =
    Def.sequential(
      Def.taskDyn {
        (docs / mdoc).toTask(s" --include ${readmeFileName.value}")
      },
      Def.task {
        val name = readmeFileName.value
        val out = (docs / mdocOut).value / name
        val readme = baseDirectory.value / name
        IO.copy(Seq(out -> readme))
        readme
      })

  private val commitReadme = ReleaseStep { st =>
    val x = Project.extract(st)
    val vcs = x.get(releaseVcs).getOrElse(
      sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
    val sign = x.get(releaseVcsSign)
    val signOff = x.get(releaseVcsSignOff)
    val name = x.get(readmeFileName)
    vcs.add(name) ! st.log
    val status = vcs.status.!!.trim
    if (status.contains(name)) {
      vcs.commit(s"Update $name", sign, signOff) ! st.log
    }
    st
  }

}
