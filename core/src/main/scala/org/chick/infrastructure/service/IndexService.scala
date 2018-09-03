package org.chick.infrastructure.service

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import org.chick.model.{IndexItem, SearchResponse}
import org.chick.infrastructure.JsonImplicits._

trait IndexService {

  def init(): IO[Option[_]]

  def add(items: Seq[IndexItem]): IO[Int]

  def query(q: String): IO[Seq[IndexItem]]

  def search(q: String) = {
    for {
      result <- query(q)
      response <- IO {
        result.map(
          item =>
            SearchResponse(item.title,
                           item.url,
                           item.body.slice(0, 100).trim,
                           item.itemType,
                           item.tags,
                           false))
      }
    } yield response.toList.take(50).asJson
  }
}
