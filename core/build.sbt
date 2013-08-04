name := "configs"

description := "A Scala wrapper for Typesafe config"

libraryDependencies ++= Seq(
  Dependencies.config,
  Dependencies.scalaTest % "test",
  Dependencies.scalaCheck % "test"
)

Publish.settings