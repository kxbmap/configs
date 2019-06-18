name := "configs-root"

enablePlugins(Unpublished)

lazy val core = project
  .settings(
    name := "configs",
    compileOrder := CompileOrder.JavaThenScala,
    Compile / unmanagedSourceDirectories ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11 | 12)) => Seq((Compile / sourceDirectory).value / "scala-2.11_2.12")
        case _ => Nil
      }
    },
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
    tutSourceDirectory := (sourceDirectory in Tut).value / "doc",
    libraryDependencies ++= Seq(
      lombok % Tut
    )
  )
