import sbt._, Keys._

object Publish {
  val settings = Seq[Setting[_]](
    publishMavenStyle := true,
    publishTo <<= version { v =>
      if (v.trim.endsWith("SNAPSHOT"))
        Some(Opts.resolver.sonatypeSnapshots)
      else
        Some(Opts.resolver.sonatypeStaging)
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
