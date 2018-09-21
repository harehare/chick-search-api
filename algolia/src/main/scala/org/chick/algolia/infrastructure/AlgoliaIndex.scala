package org.chick.algolia.infrastructure

import algolia.AlgoliaClient
import algolia.AlgoliaDsl._
import algolia.objects.Query
import algolia.responses.{SearchResult, TasksSingleIndex}
import cats.effect.Async
import org.chick.model.IndexItem
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class AlgoliaIndex[F[_]](implicit F: Async[F]) {

  val indexName = "chick-index"
  val client = new AlgoliaClient(sys.env("APPLICATION_ID"), sys.env("API_KEY"))

  def add(items: Seq[IndexItem]): F[TasksSingleIndex] =
    lift(client.execute {
      index into indexName objects items.map(x =>
        x.copy(createdAt = Some(System.currentTimeMillis)))
    })

  def query(q: String): F[SearchResult] =
    lift(client.execute {
      search into indexName query Query(query = Some(q))
    })

  def lift[A](fa: => Future[A]): F[A] =
    F.async { cb =>
      fa.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }
}
