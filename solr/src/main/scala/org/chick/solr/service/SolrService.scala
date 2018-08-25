package org.chick.solr.service

import cats.effect.IO
import mouse.option._
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, ItemType}
import org.chick.solr.infrastructure.SolrIndex

class SolrService extends IndexService {

  implicit val indexName = "chick"

  override def add(items: Seq[IndexItem]): IO[Int] = {
    for {
      count <- SolrIndex.add(items)
    } yield count
  }

  override def query(q: String): IO[Seq[IndexItem]] =
    for {
      searchResult <- SolrIndex.query(q)
      items <- IO(
        searchResult.documents.map(x =>
          IndexItem(
            x.get("title_txt_cjk").getOrElse("").toString,
            x.get("url_str").getOrElse("").toString,
            x.get("body_txt_cjk").getOrElse("").toString,
            ItemType(x.get("itemType_str").getOrElse("").toString),
            Some(x.get("tags_str").cata(x => List(x.toString), Nil)),
            Some(x
              .get("createdAt_l")
              .cata(_.toString.toLong, System.currentTimeMillis))
        )))
    } yield items

  override def init(): IO[Option[Unit]] = IO.pure(None)
}
