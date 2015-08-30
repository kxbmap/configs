name := "configs"

disablePublishSettings

lazy val core = project.settings(
  name := "configs",
  dependencies.core,
  scalapropsSettings,
  initialCommands :=
    """import com.typesafe.config._
      |import ConfigFactory._
      |import com.github.kxbmap.configs.{Configs, Bytes}
      |import com.github.kxbmap.configs.simple._
      |import com.github.kxbmap.configs.syntax._
      |""".stripMargin
).dependsOn(
  macros % "provided"
)

lazy val macros = project.settings(
  name := "configs-macro",
  dependencies.macros,
  scalapropsSettings
)
