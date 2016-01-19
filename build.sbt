name := "configs"

disablePublishSettings

lazy val core = project
  .settings(
    name := "configs",
    dependencies.core,
    scalapropsWithScalazlaws,
    initialCommands :=
      """import com.typesafe.config._
        |import ConfigFactory._
        |import configs.{Attempt, Bytes, Configs}
        |import configs.syntax.attempt._
        |""".stripMargin
  )

lazy val sample = project
  .settings(
    name := "configs-sample",
    disablePublishSettings
  )
  .dependsOn(core)
