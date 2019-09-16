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
    scalapropsWithScalaz,
    initialCommands :=
      """import com.typesafe.config.ConfigFactory._
        |import configs._
        |import configs.syntax._
        |""".stripMargin
  )

lazy val docs = project
  .dependsOn(core % Tut)
  .enablePlugins(TutPlugin, Unpublished)
  .settings(
    tutSourceDirectory := (Tut / sourceDirectory).value / "doc",
    libraryDependencies ++= Seq(
      lombok % Tut
    )
  )
