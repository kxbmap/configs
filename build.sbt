name := "configs-root"

enablePlugins(Unpublished)

lazy val core = project
  .settings(
    name := "configs",
    compileOrder := CompileOrder.JavaThenScala,
    libraryDependencies ++= Seq(
      typesafeConfig,
      scalaCollectionCompact,
      scalaReflect.value % Provided,
      lombok % Test
    ),
    scalapropsWithScalazlaws,
    initialCommands :=
      """import com.typesafe.config.ConfigFactory._
        |import configs._
        |import configs.syntax._
        |""".stripMargin
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
