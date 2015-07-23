publish := {}
publishLocal := {}
publishM2 := {}
publishArtifact := false

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild := {
  if (isSnapshot.value)
    Some(Opts.resolver.sonatypeSnapshots)
  else
    Some(Opts.resolver.sonatypeStaging)
}

licenses in ThisBuild := Seq(
  "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
)

scmInfo in ThisBuild := Some(ScmInfo(
  browseUrl = url(s"https://github.com/kxbmap/${(name in LocalRootProject).value}"),
  connection = s"scm:git:git@github.com:kxbmap/${(name in LocalRootProject).value}.git"
))

homepage in ThisBuild := Some(url(s"https://github.com/kxbmap/${(name in LocalRootProject).value}"))

organizationHomepage in ThisBuild := Some(url("https://github.com/kxbmap"))

pomIncludeRepository in ThisBuild := { _ => false }

developers in ThisBuild := List(
  Developer("kxbmap", "Tsukasa Kitachi", "kxbmap@gmail.com", url("https://github.com/kxbmap"))
)
