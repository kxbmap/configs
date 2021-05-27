name := "configs-root"

enablePlugins(Release, Unpublished)

lazy val core = project
  .settings(
    name := "configs",
    compileOrder := CompileOrder.JavaThenScala,
    libraryDependencies ++= Seq(
      typesafeConfig,
      commonsText,
      scalaCollectionCompat,
      lombok % Test
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => scalaReflect.value % Provided :: Nil
        case _ => Nil
      }
    },
    scalapropsWithScalaz,
    initialCommands :=
      """import com.typesafe.config.ConfigFactory._
        |import configs._
        |import configs.syntax._
        |""".stripMargin
  )

lazy val docs = project
  .dependsOn(core)
  .enablePlugins(MdocPlugin, Unpublished)
  .settings(
    mdocIn := (Compile / sourceDirectory).value / "mdoc",
    mdocVariables ++= Map(
      "VERSION" -> version.value
    ),
    libraryDependencies ++= Seq(lombok)
  )
