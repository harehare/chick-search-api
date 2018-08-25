package org.chick.algolia.service

import cats.effect.IO
import cats.implicits._
import org.chick.algolia.infrastructure.AlgoliaIndex
import org.chick.infrastructure.service.IndexService
import org.chick.infrastructure.ShowImplicits._
import org.chick.model.{IndexItem, ItemType}
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, JObject, _}

class AlgoliaIndexService extends IndexService {

  implicit val jsonFormats = DefaultFormats + new IndexItemSerializer()

  override def add(items: Seq[IndexItem]): IO[Int] = {
    for {
      result <- AlgoliaIndex.add(items)
    } yield result.objectIDs.length
  }

  override def query(q: String): IO[Seq[IndexItem]] =
    for {
      searchResult <- AlgoliaIndex.query(q)
      items <- IO(searchResult.hits.map(x => x.e.extract[IndexItem]))
    } yield items

  override def init(): IO[Option[Unit]] = IO.pure(None)
}

class IndexItemSerializer
    extends CustomSerializer[IndexItem](format =>
      ({
        case jObj: JObject =>
          implicit val fmt = format
          val title = (jObj \ "title").extract[String]
          val url = (jObj \ "url").extract[String]
          val body = (jObj \ "body").extract[String]
          val itemType = ItemType((jObj \ "itemType").extract[String])
          val tags = (jObj \ "tags").extract[Option[List[String]]]
          val createdAt = (jObj \ "createdAt").extract[Option[Long]]

          IndexItem(title, url, body, itemType, tags, createdAt)
      }, {
        case indexItem: IndexItem =>
          ("title" -> indexItem.title) ~
            ("url" -> indexItem.url) ~
            ("body" -> indexItem.body) ~
            ("itemType" -> indexItem.itemType.show) ~
            ("tags" -> indexItem.tags) ~
            ("createdAt" -> indexItem.createdAt)
      })) {}
