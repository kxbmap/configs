name := "configs-root"

enablePlugins(Unpublished)

lazy val core = project
  .dependsOn(testutil % "test")
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

lazy val bench = project
  .dependsOn(core, testutil)
  .enablePlugins(JmhPlugin, Unpublished)
  .settings(
    // Workaround
    extrasVersion in Jmh := "0.2.11"
  )

lazy val testutil = project
  .enablePlugins(Unpublished)
  .settings(
    Dependencies.testutil
  )

lazy val docs = project
  .dependsOn(core % "test")
  .enablePlugins(Unpublished)
  .settings(
    Dependencies.docs,
    tutSettings,
    tutSourceDirectory := sourceDirectory.value / "tut"
  )
