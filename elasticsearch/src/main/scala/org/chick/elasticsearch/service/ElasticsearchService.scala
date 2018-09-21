package org.chick.elasticsearch.service

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.sksamuel.elastic4s.{Hit, HitReader}
import org.chick.elasticsearch.infrastructure.ElasticsearchIndex
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, ItemType}
import scala.util.Try

class ElasticsarchService[F[_]](implicit F: Sync[F]) extends IndexService[F] {

  implicit val indexName = "chick"

  val oldVersion = "chick_001"
  val currentVersion = "chick_001"

  implicit object IndexItemHitReader extends HitReader[IndexItem] {
    override def read(hit: Hit): Try[IndexItem] =
      Try(
        IndexItem(
          hit.sourceAsMap("title").toString,
          hit.sourceAsMap("url").toString,
          hit.sourceAsMap("body").toString,
          ItemType(hit.sourceAsMap("itemType").toString),
          None,
          Some(hit.sourceAsMap("createdAt").toString.toLong)
        ))
  }

  override def add(items: Seq[IndexItem]): F[Int] =
    F.delay {
      ElasticsearchIndex.add(items)
      items.length
    }

  override def query(q: String): F[Seq[IndexItem]] =
    F.delay {
      ElasticsearchIndex.query(q).hits.hits.map(x => x.to[IndexItem])
    }

  override def init(): F[Boolean] =
    for {
      create <- F.delay{
        ElasticsearchIndex.create(currentVersion)
      }
      alias <- F.delay{
        ElasticsearchIndex.switchAliases(oldVersion, currentVersion)
      }
    } yield (create.acknowledged && alias.acknowledged)
}
