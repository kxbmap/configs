import sbt.Keys._
import sbt._

object Publish extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val disablePublishSettings: Seq[Setting[_]] = Seq(
      publishArtifact := false,
      publish := {},
      publishLocal := {},
      publishM2 := {}
    )
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    publishTo := {
      if (isSnapshot.value)
        Some(Opts.resolver.sonatypeSnapshots)
      else
        Some(Opts.resolver.sonatypeStaging)
    },
    licenses := Seq(
      "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    scmInfo := Some(ScmInfo(
      browseUrl = url(s"https://github.com/kxbmap/${(name in LocalRootProject).value}"),
      connection = s"scm:git:git@github.com:kxbmap/${(name in LocalRootProject).value}.git"
    )),
    homepage := Some(url(s"https://github.com/kxbmap/${(name in LocalRootProject).value}")),
    organizationHomepage := Some(url("https://github.com/kxbmap")),
    pomIncludeRepository := { _ => false },
    developers := List(
      Developer("kxbmap", "Tsukasa Kitachi", "kxbmap@gmail.com", url("https://github.com/kxbmap"))
    )
  )

}
