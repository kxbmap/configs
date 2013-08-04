import sbt._, Keys._

object ConfigsBuild extends Build {

  override val settings = super.settings ++ Seq(
    version       := "0.2.0-SNAPSHOT",
    organization  := "com.github.kxbmap",
    scalaVersion  := "2.10.2",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")
  )
}
