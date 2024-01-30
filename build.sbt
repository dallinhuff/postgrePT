ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

resolvers += "openai4s packages" at "https://maven.pkg.github.com/dallinhuff/openai4s"

lazy val root = (project in file("."))
  .settings(
    name := "postgrePT",
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"         % "3.5.3",
      "org.http4s"     %% "http4s-dsl"          % "0.23.18",
      "org.http4s"     %% "http4s-ember-client" % "0.23.18",
      "org.http4s"     %% "http4s-circe"        % "0.23.19",
      "io.circe"       %% "circe-generic"       % "0.14.6",
      "io.circe"       %% "circe-literal"       % "0.14.6",
      "com.dallinhuff" %% "openai4s"            % "0.1.1-SNAPSHOT"
    )
  )
