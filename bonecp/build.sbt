name := "configs-bonecp"

description := "Configs instance for BoneCPConfig"

libraryDependencies ++= Seq(
  Dependencies.bonecp % "provided",
  Dependencies.scalaTest % "test",
  Dependencies.scalaCheck % "test",
  Dependencies.slf4j_nop % "test"
)

ideaBasePackage := Some("com.github.kxbmap.configs")
