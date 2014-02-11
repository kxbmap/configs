import Dependencies._

name := "configs-support"

description := "Configs support instances"

libraryDependencies ++= Seq(
  bonecp            % "optional",
  scalikejdbc       % "optional",
  scalikejdbcAsync  % "optional",
  scalaTest         % "test",
  scalaCheck        % "test",
  slf4j_nop         % "test",
  mysql             % "test",
  postgresql        % "test",
  h2db              % "test",
  hsqldb            % "test",
  postgresqlAsync   % "test",
  mysqlAsync        % "test"
)

parallelExecution := false

Publish.settings
