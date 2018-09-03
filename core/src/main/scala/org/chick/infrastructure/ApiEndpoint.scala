package org.chick.infrastructure

import cats.effect.IO
import cats.effect.concurrent.Ref
import io.circe.generic.auto._
import io.circe.syntax._
import mouse.boolean._
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, IndexResponse}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.chick.infrastructure.JsonImplicits._

class ApiEndpoint(val index: IndexService, ref: Ref[IO, List[IndexItem]]) {

  object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")

  implicit val decoder = jsonOf[IO, List[IndexItem]]

  val service = HttpRoutes.of[IO] {
    case GET -> Root / "search"
          :? SearchQueryParamMatcher(q) => {
      (!Option(q)
        .getOrElse("")
        .isEmpty)
        .fold(Ok(index.search(q)),
              IO.raiseError(new Throwable("q is required.")))
    }
    case req @ POST -> Root / "index" =>
      for {
        _ <- index.init
        items <- req.as[List[IndexItem]]
        _ <- ref.modify(list => (items ++ list, list))
        res <- Ok(IndexResponse(items.length).asJson)
      } yield (res)
    case _ -> Root =>
      MethodNotAllowed()
  }
}
