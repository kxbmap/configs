import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.pgp.PgpKeys._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

object Release extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = ReleasePlugin && SbtPgp

  object autoImport {
    val readmeFile = settingKey[File]("README file")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    readmeFile := baseDirectory.value / "README.md",
    releaseCrossBuild := true,
    releasePublishArtifactsAction := publishSigned.value,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      updateReadme,
      commitReadme,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion
    )
  )

  val updateReadme = ReleaseStep { st =>
    val (releaseVer, _) = st.get(versions).getOrElse(
      sys.error("No versions are set! Was this release part executed before inquireVersions?"))
    val x = Project.extract(st)
    val readme = x.get(readmeFile)
    val dep = s"""libraryDependencies \\+= "${x.get(organization).replaceAll("\\.", "\\.")}" %% ".+" % "(.+)"""".r
    val updated = IO.read(readme).linesIterator.map {
      case line@dep(ver) => line.replace(ver, releaseVer)
      case line          => line
    }.mkString("", "\n", "\n")
    IO.write(readme, updated)
    st
  }

  val commitReadme = ReleaseStep { st =>
    val x = Project.extract(st)
    val vcs = x.get(releaseVcs).getOrElse(
      sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
    val base = vcs.baseDir
    val readme = x.get(readmeFile)
    val relative = IO.relativize(base, readme).getOrElse(
      sys.error(s"Readme file [$readme] is outside of this VCS repository with base directory [$base]!"))

    vcs.add(relative) ! st.log
    val status = vcs.status.!!.trim
    if (status.nonEmpty) {
      vcs.commit(s"Update $relative") ! st.log
    }
    st
  }

}
