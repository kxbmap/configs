name := "configs-root"

enablePlugins(Unpublished)

lazy val core = project
  .dependsOn(testutil % "test")
  .settings(
    name := "configs",
    Dependencies.core,
    scalapropsWithScalazlaws,
    initialCommands :=
      """import com.typesafe.config.ConfigFactory._
        |import configs._
        |import configs.syntax._
        |""".stripMargin
  )

lazy val bench = project
  .dependsOn(core, testutil)
  .enablePlugins(JmhPlugin, Unpublished)

lazy val testutil = project
  .enablePlugins(Unpublished)
  .settings(
    Dependencies.testutil
  )

lazy val docs = project
  .dependsOn(core % "tut")
  .enablePlugins(TutPlugin, Unpublished)
  .settings(
    Dependencies.docs,
    tutSourceDirectory := (sourceDirectory in Tut).value / "doc"
  )
