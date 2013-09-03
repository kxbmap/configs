import Dependencies._

name := "configs-support"

description := "Configs support instances"

libraryDependencies ++= Seq(
  bonecp % "optional",
  scalikejdbc      % "optional",
  scalikejdbcAsync % "optional",
  scalaTest  % "test",
  scalaCheck % "test",
  slf4j_nop  % "test"
)

ideaBasePackage := Some("com.github.kxbmap.configs")
