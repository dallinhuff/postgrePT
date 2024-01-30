package com.dallinhuff.postgrespt

import cats.effect.*
import com.dallinhuff.postgrespt.prompt.{CompletionMessage, CompletionRequest}
import org.http4s.*
import org.http4s.ember.client.*
import org.http4s.client.Client

object Main extends IOApp.Simple:
  val request: Request[IO] =
    CompletionRequest(
      "gpt-3.5-turbo",
      List(
        CompletionMessage(
          "system",
          "You are a program that generates fully-formed SQL queries from natural language prompts. " +
            "Respond to prompts from the user in only fully-formed SQL queries with no explanations"
        ),
        CompletionMessage(
          "user",
          "get all of the rows from the user table"
        )
      )
    )

  val clientResource: Resource[IO, Client[IO]] =
    EmberClientBuilder
      .default[IO]
      .build

  def sendRequest(client: Client[IO])(request: Request[IO]): IO[Unit] =
    client
      .expect[String](request)
      .flatMap(IO.println)

  val run: IO[Unit] =
    for
      _ <- clientResource.use(sendRequest(_)(request))
    yield ()