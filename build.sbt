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

def extension(id: String): Project = Project(id, file("ext") / id)
  .settings(name := s"configs-$id")
  .settings(commonSettings ++ publishSettings)
  .dependsOn(core % "compile->compile;test->test")

lazy val std = extension("std")

lazy val scalikejdbc = extension("scalikejdbc").settings(
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % "2.2.6",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41" % "test",
    "mysql" % "mysql-connector-java" % "5.1.35" % "test",
    "com.h2database" % "h2" % "1.4.187" % "test",
    "org.hsqldb" % "hsqldb" % "2.3.2" % "test"
  )
)

lazy val scalikejdbcAsync = extension("scalikejdbc-async").settings(
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc-async" % "0.5.5",
    "com.github.mauricio" %% "postgresql-async" % "0.2.16" % "test",
    "com.github.mauricio" %% "mysql-async" % "0.2.16" % "test"
  )
)

lazy val bonecp = extension("bonecp").settings(
  libraryDependencies ++= Seq(
    "com.jolbox" % "bonecp" % "0.8.0.RELEASE"
  )
)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6",
  organization := "com.github.kxbmap",
  crossScalaVersions := Seq(scalaVersion.value, "2.10.5"),
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
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
    "org.slf4j" % "slf4j-simple" % "1.7.12" % "test"
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
