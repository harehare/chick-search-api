package org.chick.infrastructure

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import mouse.boolean._
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, IndexResponse}
import org.chick.infrastructure.JsonImplicits._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext.Implicits.global

class ApiEndpoint(val index: IndexService) {

  object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")

  implicit val decoder = jsonOf[IO, List[IndexItem]]

  val service = HttpRoutes.of[IO] {
    case GET -> Root / "search"
          :? SearchQueryParamMatcher(q) => {
      (!Option(q)
        .getOrElse("")
        .isEmpty)
        .fold(Ok(query(q)), IO.raiseError(new Throwable("q is required.")))
    }
    case req @ POST -> Root / "index" =>
      for {
        _ <- index.init
        items <- req.as[List[IndexItem]]
        count <- index.add(items)
        res <- Ok(IndexResponse(count).asJson)
      } yield (res)
    case _ -> Root =>
      MethodNotAllowed()
  }

  def query(q: String) = {
    for {
      searchResult <- index.query(q)
    } yield searchResult.take(50).asJson
  }
}
