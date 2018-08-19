package org.chick.solr.infrastructure

import cats.effect.IO
import com.github.takezoe.solr.scala.{MapQueryResult, Order}
import com.github.takezoe.solr.scala.async.AsyncSolrClient
import org.chick.model.IndexItem

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object SolrIndex {
  import com.github.takezoe.solr.scala._

  val client = new AsyncSolrClient(sys.env("SOLR_URL"))

  def add(items: Seq[IndexItem]): IO[Seq[Unit]] =
    IO.async { cb =>
      Future
        .traverse(items) { x =>
          client
            .add(Map(
              "title_txt_cjk" -> x.title,
              "url_str" -> x.url,
              "body_txt_cjk" -> x.body,
              "itemType_str" -> x.itemType.name,
              "tags_str" -> x.tags,
              "createdAt_l" -> System.currentTimeMillis
            ))
        }
        .onComplete {
          case Success(a) => {
            client.commit
            cb(Right(a))
          }
          case Failure(e) => cb(Left(e))
        }
    }

  def query(q: String): IO[MapQueryResult] =
    liftIO(
      client
        .query("body_txt_cjk:%q%^10.0 OR title_txt_cjk:%q%")
        .fields("title_txt_cjk",
                "url_str",
                "body_txt_cjk",
                "itemType_str",
                "tags_str",
                "createdAt_l")
        .sortBy("score", Order.desc)
        .getResultAsMap(Map("q" -> q)))
}
