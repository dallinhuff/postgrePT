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
          "You are a poetic assistant, skilled in explaining complex programming concepts with creative flair"
        ),
        CompletionMessage(
          "user",
          "Compose a poem that explains the concept of recursion in programming"
        )
      )
    )

  def sendRequest(client: Client[IO])(request: Request[IO]): IO[Unit] =
    client
      .expect[String](request)
      .flatMap(IO.println)

  val run: IO[Unit] =
    EmberClientBuilder
      .default[IO]
      .build
      .use(client => sendRequest(client)(request))