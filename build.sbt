ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val openai4s = RootProject(uri("https://github.com/dallinhuff/openai4s.git#v0.1.2-SNAPSHOT"))

lazy val root = (project in file("."))
  .settings(
    name := "postgrePT",
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"         % "3.5.3",
      "org.http4s"     %% "http4s-dsl"          % "0.23.18",
      "org.http4s"     %% "http4s-ember-client" % "0.23.18",
      "org.http4s"     %% "http4s-circe"        % "0.23.19",
      "org.tpolecat"   %% "skunk-core"          % "0.6.2",
      "io.circe"       %% "circe-generic"       % "0.14.6",
      "io.circe"       %% "circe-literal"       % "0.14.6",
      "org.postgresql" % "postgresql"           % "42.5.4"
    )
  )
  .dependsOn(openai4s)
