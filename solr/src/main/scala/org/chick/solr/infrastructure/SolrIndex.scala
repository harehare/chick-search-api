package org.chick.solr.infrastructure

import cats.effect.Async
import com.github.takezoe.solr.scala.async.AsyncSolrClient
import org.chick.model.IndexItem
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class SolrIndex[F[_]](implicit F: Async[F]) {
  import com.github.takezoe.solr.scala._

  val client = new AsyncSolrClient(sys.env("SOLR_URL"))

  def add(items: Seq[IndexItem]): F[Int] =
    F.async{ cb =>
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
          case Success(_) => cb(Right(items.length))
          case Failure(e) => cb(Left(e))
        }
    }

  def query(q: String): F[MapQueryResult] =
    lift(
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

  def lift[A](fa: => Future[A]): F[A] =
    F.async { cb =>
      fa.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }
}
