name := "configs"

disablePublishSettings

lazy val core = project.settings(
  name := "configs",
  dependencies.core,
  scalapropsSettings,
  initialCommands :=
    """import com.typesafe.config._
      |import ConfigFactory._
      |import configs.{Configs, Bytes}
      |import configs.syntax._
      |""".stripMargin
)
