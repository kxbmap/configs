name := "configs-root"

enablePlugins(Unpublished)

lazy val core = project
  .settings(
    name := "configs",
    Dependencies.core,
    scalapropsWithScalazlaws,
    compileOrder in Test := CompileOrder.JavaThenScala,
    initialCommands :=
      """import com.typesafe.config.ConfigFactory._
        |import configs._
        |import configs.syntax._
        |""".stripMargin
  )

lazy val doc = project
  .enablePlugins(Unpublished)
  .settings(
    name := "configs-doc",
    Dependencies.doc,
    tutSettings,
    tutSourceDirectory := sourceDirectory.value / "tut"
  )
  .dependsOn(core % "test")
