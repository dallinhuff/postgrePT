ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "postgrePT",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"         % "3.5.3",
      "org.http4s"    %% "http4s-dsl"          % "0.23.18",
      "org.http4s"    %% "http4s-ember-client" % "0.23.18",
      "org.http4s" %% "http4s-circe" % "0.23.19",
      // Optional for auto-derivation of JSON codecs
      "io.circe" %% "circe-generic" % "0.14.6",
      // Optional for string interpolation to JSON model
      "io.circe" %% "circe-literal" % "0.14.6"
    )
  )
