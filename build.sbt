name := "root"

lazy val core = project
lazy val support = project dependsOn core

version in Global := "0.2.1-SNAPSHOT"

organization in Global := "com.github.kxbmap"

scalaVersion in Global := "2.10.2"

scalacOptions in Global ++= Seq("-feature", "-unchecked", "-deprecation")

inScope(Global)(Publish.settings)

publish := {}

publishLocal := {}

publishArtifact := false
