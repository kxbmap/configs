name := "configs-bonecp"

description := "Configs instance for BoneCPConfig"

libraryDependencies ++= Seq(
  Dependencies.bonecp % "provided",
  Dependencies.scalaTest % "test",
  Dependencies.scalaCheck % "test"
)

Publish.settings

ideaBasePackage := Some("com.github.kxbmap.configs")
