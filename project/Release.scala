import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.pgp.PgpKeys._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version.Bump
import tut.Plugin._

object Release extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = ReleasePlugin && SbtPgp

  object autoImport {
    val readmeFileName = settingKey[String]("Readme file name")
    val readmeFileSource = settingKey[File]("README file source")
    val readmeFile = settingKey[File]("README file")
    val updateReadme = taskKey[File]("Update readme file")
  }

  import autoImport._

  private val doc = LocalProject("doc")

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    readmeFileName := "README.md",
    readmeFileSource := (tutSourceDirectory in doc).value / readmeFileName.value,
    readmeFile := (baseDirectory in LocalRootProject).value / readmeFileName.value,
    updateReadme <<= updateReadmeTask,
    releaseCrossBuild := true,
    releasePublishArtifactsAction := publishSigned.value,
    releaseVersionBump := Bump.Minor,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      updateReadmeStep,
      commitReadme,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion
    )
  )

  private def updateReadmeTask = Def.task {
    val name = readmeFileName.value
    val out = (tut in doc).value
      .collectFirst { case (o, `name`) => o }
      .getOrElse(sys.error(s"$name not found"))
    val readme = readmeFile.value
    IO.copy(Seq(out -> readme))
    readme
  }

  val updateReadmeStep = ReleaseStep { st =>
    val (releaseVer, _) = st.get(versions).getOrElse(
      sys.error("No versions are set! Was this release part executed before inquireVersions?"))
    val x = Project.extract(st)
    val org = x.get(organization).replaceAll("\\.", "\\.")
    val dep = s"""libraryDependencies \\+= "$org" %% ".+" % "(.+)"""".r
    val src = x.get(readmeFileSource)
    val updated = IO.read(src).linesIterator.map {
      case line@dep(ver) => line.replace(ver, releaseVer)
      case line => line
    }.mkString("", "\n", "\n")
    IO.write(src, updated)
    x.runTask(updateReadme, st)._1
  }

  val commitReadme = ReleaseStep { st =>
    val x = Project.extract(st)
    val vcs = x.get(releaseVcs).getOrElse(
      sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
    val base = vcs.baseDir
    val files = Seq(x.get(readmeFile), x.get(readmeFileSource)).map { f =>
      IO.relativize(base, f).getOrElse(
        sys.error(s"Readme file [$f] is outside of this VCS repository with base directory [$base]!"))
    }
    vcs.add(files: _*) ! st.log
    val status = vcs.status.!!.trim
    if (status.nonEmpty) {
      vcs.commit(s"Update ${x.get(readmeFileName)}") ! st.log
    }
    st
  }

}
