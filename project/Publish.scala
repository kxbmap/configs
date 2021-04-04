import com.jsuereth.sbtpgp.PgpKeys._
import com.jsuereth.sbtpgp.SbtPgp
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.GitHubHosting
import xerial.sbt.Sonatype.SonatypeKeys._

object Publish extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = Common && Sonatype && ReleasePlugin && SbtPgp

  override def projectSettings: Seq[Setting[_]] = Seq(
    description := "Scala wrapper for Typesafe config",
    organization := "com.github.kxbmap",
    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    sonatypeProjectHosting := Some(GitHubHosting("kxbmap", "configs", "Tsukasa Kitachi", "kxbmap@gmail.com")),
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    pomIncludeRepository := { _ => false },
    releasePublishArtifactsAction := publishSigned.value
  )

}

object Unpublished extends AutoPlugin {

  override def requires: Plugins = plugins.IvyPlugin

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    publish / skip := true,
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

}
