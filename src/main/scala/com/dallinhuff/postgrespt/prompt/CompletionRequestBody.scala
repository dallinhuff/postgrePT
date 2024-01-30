package com.dallinhuff.postgrespt.prompt

case class CompletionRequestBody(
  model: String,
  messages: List[CompletionMessage],
  stream: Boolean = true
)
