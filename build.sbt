version in ThisBuild := "0.3.0-SNAPSHOT"

commonSettings
rootPublishSettings

lazy val core = project.settings(commonSettings ++ publishSettings).settings(
  name := "configs-core",
  description := "A Scala wrapper for Typesafe config",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.0"
  )
)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6",
  organization := "com.github.kxbmap",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-Xexperimental",
    "-language:higherKinds"
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
  )
)

lazy val publishSettings = Seq(
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
    browseUrl = url("http://github.com/kxbmap/configs"),
    connection = "scm:git:git@github.com:kxbmap/configs.git"
  )),
  homepage := Some(url("http://github.com/kxbmap/configs")),
  organizationHomepage := Some(url("http://github.com/kxbmap")),
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <developers>
      <developer>
        <id>kxbmap</id>
        <name>Tsukasa Kitachi</name>
        <url>http://github.com/kxbmap</url>
      </developer>
    </developers>
)

lazy val rootPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)
