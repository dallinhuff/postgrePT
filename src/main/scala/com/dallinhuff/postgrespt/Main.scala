package com.dallinhuff.postgrespt

import cats.effect.*
import com.dallinhuff.openai4s.auth.OpenAIKey
import com.dallinhuff.openai4s.OpenAIClient
import com.dallinhuff.openai4s.entities.chat
import com.dallinhuff.openai4s.entities.chat.*
import com.dallinhuff.postgrespt.prompt.*

import java.sql.{Connection, DriverManager, ResultSet, Statement}

object Main extends IOApp.Simple:

  private val key = OpenAIKey(Option(System.getenv("OPEN_AI_KEY")).getOrElse(""))

  def run: IO[Unit] =
    repl.foreverM

  private val repl: IO[Unit] =
    for
      _ <- IO.println("ask me something about your makeup:")
      question <- IO.readLine
      _ <- IO.println("generating query...")
      query <- generateQuery(question)
      _ <- IO.println(s"Query:\n$query\n")
      _ <- IO.println("checking db...")
      result <- IO.blocking(sendQuery(query))
      resultString <- IO.pure(result.mkString("[", ", ", "]"))
      interpreted <- interpretResult(question, query, resultString)
      _ <- IO.println(s"$interpreted\n\n")
    yield ()

  /**
   * ask chat to generate a sql query
   * @param question the natural-language question to ask
   * @return IO-wrapped string of the generated query
   */
  private def generateQuery(question: String): IO[String] =
    for
      key <- IO.pure(key)
      req <- IO.pure:
        CreateChat(
          model = "gpt-3.5-turbo",
          messages = List(
            ChatMessage.System(
              s"$generateInstructions\n\nHere are the table definitions in the database:\n\n$tables"
            ),
            ChatMessage.User(question)
          )
        )
      res <- OpenAIClient.getChatCompletion(req)(using key)
      msg <- IO.pure:
        res.choices.head.message match
          case ChatMessage.Assistant(content, _, _) => content.getOrElse("")
          case _ => ""
    yield msg

  /**
   * ask chat to interpret the result of hitting the db
   * @param question the question we originally asked
   * @param query the generated query
   * @param result a json-like representation of what the db returned
   * @return an IO-wrapped string of chat's interpretation of the result
   */
  private def interpretResult(question: String, query: String, result: String): IO[String] =
    for
      key <- IO.pure(key)
      req <- IO.pure:
        CreateChat(
          model = "gpt-3.5-turbo",
          messages = List(
            ChatMessage.User(interpretInstructions(question, query, result))
          )
        )
      res <- OpenAIClient.getChatCompletion(req)(using key)
      msg <- IO.pure:
        res.choices.head.message match
          case ChatMessage.Assistant(content, _, _) => content.getOrElse("")
          case _ => ""
    yield msg

  /**
   * some really gross Java-style JDBC access since
   * all of the scala sql libraries are really type-safe
   * and won't let us execute arbitrary sql strings
   * @param query the chat-generated sql query
   * @return a list of rows as strings in the table
   */
  private def sendQuery(query: String): List[String] =
    val url = "jdbc:postgresql://localhost:5432/postgres"
    val username = "docker"
    val password = "docker"

    var connection: Connection = null
    var statement: Statement = null
    var resultSet: ResultSet = null

    try
      Class.forName("org.postgresql.Driver")

      connection = DriverManager.getConnection(url, username, password)
      statement = connection.createStatement()

      resultSet = statement.executeQuery(query)

      val metaData = resultSet.getMetaData
      val columnCount = metaData.getColumnCount

      var results = List.empty[String]

      while resultSet.next() do
        val rowStringBuilder = new StringBuilder()
        for (i <- 1 to columnCount)
          rowStringBuilder.append("{")
          rowStringBuilder.append(s"\"${metaData.getColumnName(i)}\": ")
          rowStringBuilder.append(s"\"${resultSet.getString(i)}\", ")
          rowStringBuilder.append("}\n")
          results = rowStringBuilder.result() :: results
      results.reverse
    finally
      if (resultSet != null) resultSet.close()
      if (statement != null) statement.close()
      if (connection != null) connection.close()