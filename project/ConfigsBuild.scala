import sbt._, Keys._
import org.sbtidea.SbtIdeaPlugin._

object ConfigsBuild extends Build {

  lazy val baseSettings = seq(
    version       := "0.2.0-SNAPSHOT",
    organization  := "com.github.kxbmap",
    scalaVersion  := "2.10.2",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")
  )


  lazy val root = Project(
    id    = "root",
    base  = file(".")
  ).settings(
    baseSettings ++ publishSettings:_*
  ).settings(
    name            := "configs",
    description     := "A Scala wrapper for Typesafe config",
    publishArtifact := false
  ).dependsOn(
    core
  ).aggregate(
    core, ext, sample
  )


  lazy val core = Project(
    id    = "core",
    base  = file("core")
  ).settings(
    baseSettings ++ publishSettings:_*
  ).settings(
    name        := "configs-core",
    description := "A Scala wrapper for Typesafe config (core)",

    libraryDependencies += "com.typesafe" % "config" % "1.0.1",
    libraryDependencies ++= testDependencies
  )


  lazy val ext = Project(
    id    = "ext",
    base  = file("ext")
  ).settings(
    baseSettings ++ publishSettings:_*
  ).settings(
    name        := "configs-ext",
    description := "A Scala wrapper for Typesafe config (ext)",

    libraryDependencies ++= Seq(
      "com.jolbox" % "bonecp" % "0.7.1.RELEASE" % Provided
    ),
    libraryDependencies ++= testDependencies,

    ideaBasePackage := Some("com.github.kxbmap.configs")
  ).dependsOn(core)


  lazy val sample = Project(
    id    = "sample",
    base  = file("sample")
  ).settings(
    baseSettings ++ publishSettings:_*
  ).settings(
    name        := "configs-sample",
    description := "A Scala wrapper for Typesafe config (sample)"
  ).dependsOn(core)


  lazy val testDependencies = Seq(
    "org.scalatest"   %% "scalatest"  % "1.9.1"   % Test,
    "org.scalacheck"  %% "scalacheck" % "1.10.1"  % Test
  )

  lazy val publishSettings = seq(
    publishMavenStyle := true,
    publishTo <<= version { v =>
      if (v.trim.endsWith("SNAPSHOT"))
        Some(Resolver.sonatypeRepo("snapshots"))
      else
        Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    },
    licenses := Seq(
      "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    scmInfo := Some(ScmInfo(
      browseUrl  = url("http://github.com/kxbmap/configs"),
      connection = "scm:git:git@github.com:kxbmap/configs.git"
    )),
    homepage              := Some(url("http://github.com/kxbmap/configs")),
    organizationHomepage  := Some(url("http://github.com/kxbmap")),
    pomIncludeRepository  := { _ => false },
    pomExtra :=
      <developers>
        <developer>
          <id>kxbmap</id>
          <name>Tsukasa Kitachi</name>
          <url>http://github.com/kxbmap</url>
        </developer>
      </developers>
  )

}
