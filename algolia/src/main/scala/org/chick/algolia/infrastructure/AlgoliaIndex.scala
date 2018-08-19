package org.chick.algolia.infrastructure

import algolia.AlgoliaClient
import algolia.AlgoliaDsl._
import algolia.objects.Query
import algolia.responses.{SearchResult, TasksSingleIndex}
import cats.effect.IO
import org.chick.model.IndexItem

import scala.concurrent.ExecutionContext.Implicits.global

object AlgoliaIndex {

  val indexName = "chick-index"
  val client = new AlgoliaClient(sys.env("APPLICATION_ID"), sys.env("API_KEY"))

  def add(items: Seq[IndexItem]): IO[TasksSingleIndex] =
    liftIO(client.execute {
      index into indexName objects items.map(x =>
        x.copy(createdAt = Some(System.currentTimeMillis)))
    })

  def query(q: String): IO[SearchResult] =
    liftIO(client.execute {
      search into indexName query Query(query = Some(q))
    })
}
