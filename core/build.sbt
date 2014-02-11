import Dependencies._

name := "configs"

description := "A Scala wrapper for Typesafe config"

libraryDependencies ++= Seq(
  typesafeConfig,
  scalaTest  % "test",
  scalaCheck % "test"
)

Publish.settings
