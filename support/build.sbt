name := "configs-support"

description := "Configs support instances"

libraryDependencies ++= Seq(
  Dependencies.bonecp     % "optional",
  Dependencies.scalaTest  % "test",
  Dependencies.scalaCheck % "test",
  Dependencies.slf4j_nop  % "test"
)

ideaBasePackage := Some("com.github.kxbmap.configs")
