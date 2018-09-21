package org.chick.solr.service

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import mouse.option._
import org.chick.infrastructure.service.IndexService
import org.chick.model.{IndexItem, ItemType}
import org.chick.solr.infrastructure.SolrIndex

class SolrService[F[_]](implicit F: Async[F]) extends IndexService[F] {

  implicit val indexName = "chick"
  val index = new SolrIndex[F]

  override def add(items: Seq[IndexItem]): F[Int] =
    for {
      count <- index.add(items)
    } yield count

  override def query(q: String): F[Seq[IndexItem]] =
    for {
      searchResult <- index.query(q)
      items <- F.delay {
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
          ))
      }
    } yield items

  override def init(): F[Boolean] = F.pure(true)
}
