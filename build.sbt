name := "configs"

disablePublishSettings

lazy val core = project.settings(
  name := "configs-java7",
  dependencies.core,
  scalapropsSettings,
  initialCommands :=
    """import com.typesafe.config._
      |import ConfigFactory._
      |import com.github.kxbmap.configs.{Configs, Bytes}
      |import com.github.kxbmap.configs.syntax._
      |""".stripMargin
)
