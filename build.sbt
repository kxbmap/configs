name := "configs-root"

disablePublishSettings

lazy val core = project
  .settings(
    name := "configs",
    dependencies.core,
    scalapropsWithScalazlaws,
    compileOrder in Test := CompileOrder.JavaThenScala,
    initialCommands :=
      """import com.typesafe.config._
        |import ConfigFactory._
        |import configs.{Bytes, Configs, Result}
        |import configs.syntax._
        |""".stripMargin
  )

lazy val doc = project
  .settings(
    name := "configs-doc",
    dependencies.doc,
    tutSettings,
    tutSourceDirectory := sourceDirectory.value / "tut",
    disablePublishSettings
  )
  .dependsOn(core % "test")
