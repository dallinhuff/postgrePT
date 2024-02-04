package com.dallinhuff.postgrespt

import cats.effect.*
import com.dallinhuff.openai4s.auth.OpenAIKey
import com.dallinhuff.openai4s.OpenAIClient
import com.dallinhuff.openai4s.entities.chat
import com.dallinhuff.openai4s.entities.chat.*
import com.dallinhuff.postgrespt.prompt.*

import java.sql.{Connection, DriverManager, Statement}

object Main extends IOApp.Simple:

  private val key = OpenAIKey(Option(System.getenv("OPEN_AI_KEY")).getOrElse(""))

  def run: IO[Unit] = repl.foreverM

  private val repl: IO[Unit] =
    for
      _            <- IO.println("ask me something about your makeup:")
      question     <- IO.readLine
      _            <- IO.println("generating query...")
      query        <- generateQuery(question)
      _            <- IO.println(s"Query:\n$query\n")
      _            <- IO.println("checking db...")
      result       <- sendQuery(query)
      resultString <- IO.pure(result.mkString("[", ", ", "]"))
      interpreted  <- interpretResult(question, query, resultString)
      _            <- IO.println(s"$interpreted\n\n")
    yield ()

  /**
   * ask chat to generate a sql query
   *
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
   * quick IO-abstraction over JDBC since all good
   * Scala libs are type-safe and don't let you throw random
   * SQL strings at it
   *
   * @param query the SQL string to execute
   * @return a list of rows in the result set represented as json objects
   */
  private def sendQuery(query: String): IO[List[String]] =
    dbConn.use: conn =>
      dbStmt(conn).use: stmt =>
        queryResult(stmt, query).use: resultSet =>
          IO.blocking:
            val metaData = resultSet.getMetaData
            val columnCount = metaData.getColumnCount

            var results = List.empty[String]
            while resultSet.next() do
              results = (1 to columnCount)
                .map: i =>
                  s"\"${metaData.getColumnName(i)}\": \"${resultSet.getString(i)}\""
                .mkString("{", ", ", "}") :: results

            results.reverse

  private val dbConn = Resource.fromAutoCloseable:
    IO.blocking:
      val url = "jdbc:postgresql://localhost:5432/postgres"
      val username = "docker"
      val password = "docker"

      Class.forName("org.postgresql.Driver")

      DriverManager.getConnection(url, username, password)

  private def dbStmt(conn: Connection) =
    Resource.fromAutoCloseable:
      IO.blocking:
        conn.createStatement()

  private def queryResult(stmt: Statement, query: String) =
    Resource.fromAutoCloseable:
      IO.blocking:
        stmt.executeQuery(query)

  /**
   * ask chat to interpret the result of hitting the db
   *
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
