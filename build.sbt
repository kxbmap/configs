name := "root"

lazy val core = project
lazy val support = project dependsOn core

version in ThisBuild := "0.2.5-SNAPSHOT"

organization in ThisBuild := "com.github.kxbmap"

scalaVersion in ThisBuild := "2.10.5"

crossScalaVersions in ThisBuild += "2.11.7"

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  Opts.compile.unchecked,
  Opts.compile.deprecation
)

Publish.settings

publish := {}

publishLocal := {}

publishArtifact := false
