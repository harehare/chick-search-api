package org.chick.infrastructure

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.Sync
import cats.effect.concurrent.Ref
import io.circe.generic.auto._
import io.circe.syntax._
import mouse.boolean._
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, IndexResponse, SearchResponse}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.chick.JsonImplicits._

class ApiEndpoint[F[_]](val index: IndexService[F], ref: Ref[F, List[IndexItem]])(implicit F: Sync[F]) extends Http4sDsl[F] {

  object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")

  implicit val decoder = jsonOf[F, List[IndexItem]]

  val service = HttpRoutes.of[F] {
    case GET -> Root / "search"
          :? SearchQueryParamMatcher(q) => {
      (!Option(q)
        .getOrElse("")
        .isEmpty)
        .fold(Ok(search(q)),
              F.raiseError(new Throwable("q is required.")))
    }
    case req @ POST -> Root / "index" =>
      for {
        _ <- F.delay(index.init)
        items <- req.as[List[IndexItem]]
        _ <- ref.modify(list => (items ++ list, list))
        res <- Ok(IndexResponse(items.length).asJson)
      } yield (res)
  }

  def search(queryString: String)=
    for {
      items <- index.query(queryString)
      res <- F.delay{
        items.map (
          item =>
            SearchResponse (item.title,
              item.url,
              item.body.slice (0, 100).trim,
              item.itemType,
              item.tags,
              false) )
      }
    } yield (res.toList.take (50).asJson)
}
