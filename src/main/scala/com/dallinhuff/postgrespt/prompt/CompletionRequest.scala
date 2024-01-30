package com.dallinhuff.postgrespt.prompt

import io.circe.syntax.*
import io.circe.generic.auto.*
import org.http4s.headers.{Authorization, `Content-Type`}
import org.http4s.{AuthScheme, Credentials, Headers, MediaType, Method, Request}
import org.http4s.syntax.all.*
import org.http4s.circe.*

val meganKey = "TODO_FIX_ME" // TODO: use config file to load this in

object CompletionRequest:
  def apply[F[_]](model: String, messages: List[CompletionMessage]): Request[F] =
    Request[F](
      method = Method.POST,
      uri = uri"https://api.openai.com/v1/chat/completions",
      headers = Headers(
        Authorization(Credentials.Token(AuthScheme.Bearer, meganKey)),
        `Content-Type`(MediaType.application.json)
      )
    ).withEntity(CompletionRequestBody(model, messages).asJson)
