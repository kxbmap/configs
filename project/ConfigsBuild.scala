import sbt._, Keys._

object ConfigsBuild extends Build {

  lazy val configs = Project(
    id    = "configs",
    base  = file(".")
  ).settings(
    name          := "configs",
    version       := "0.2.0-SNAPSHOT",
    organization  := "com.github.kxbmap",
    description   := "A Scala wrapper for Typesafe config",

    scalaVersion  := "2.10.1",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    libraryDependencies ++= Seq(
      "com.typesafe"     % "config"     % "1.0.0",
      "org.scalatest"   %% "scalatest"  % "1.9.1"   % Test,
      "org.scalacheck"  %% "scalacheck" % "1.10.1"  % Test
    ),

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
