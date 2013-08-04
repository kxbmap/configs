name := "root"

lazy val core   = project
lazy val std    = project dependsOn core
lazy val bonecp = project dependsOn core

Publish.settings

publish := {}

publishLocal := {}

publishArtifact := false
