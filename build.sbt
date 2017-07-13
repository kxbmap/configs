name := "configs-root"

enablePlugins(Unpublished)

lazy val core = project
  .dependsOn(testutil % "test")
  .settings(
    name := "configs",
    libraryDependencies ++= Seq(
      typesafeConfig,
      scalaReflect.value % "provided",
      lombok % "test"
    ),
    scalapropsWithScalazlaws,
    initialCommands :=
      """import com.typesafe.config.ConfigFactory._
        |import configs._
        |import configs.syntax._
        |""".stripMargin
  )

lazy val testutil = project
  .enablePlugins(Unpublished)
  .settings(
    libraryDependencies ++= Seq(
      lombok
    )
  )

lazy val docs = project
  .dependsOn(core % "tut")
  .enablePlugins(TutPlugin, Unpublished)
  .settings(
    tutSourceDirectory := (sourceDirectory in Tut).value / "doc",
    libraryDependencies ++= Seq(
      lombok % "tut"
    )
  )
