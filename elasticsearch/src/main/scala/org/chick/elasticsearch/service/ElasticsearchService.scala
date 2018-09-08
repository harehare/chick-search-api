package org.chick.elasticsearch.service

import cats.effect.IO
import com.sksamuel.elastic4s.{Hit, HitReader}
import mouse.boolean._
import org.chick.elasticsearch.infrastructure.ElasticsearchIndex
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, ItemType}

import scala.util.Try

object ElasticsarchService extends IndexService {

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

  override def add(items: Seq[IndexItem]): IO[Int] = {
    for {
      _ <- ElasticsearchIndex.add(items)
    } yield items.length
  }

  override def query(q: String): IO[Seq[IndexItem]] =
    for {
      searchResult <- ElasticsearchIndex.query(q)
      items <- IO(searchResult.hits.hits.map(x => x.to[IndexItem]))
    } yield items

  override def init(): IO[Option[Boolean]] =
    for {
      create <- ElasticsearchIndex.create(currentVersion)
      alias <- ElasticsearchIndex.switchAliases(oldVersion, currentVersion)
    } yield (create.acknowledged && alias.acknowledged).fold(Some(true), None)
}
