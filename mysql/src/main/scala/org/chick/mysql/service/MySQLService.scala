package org.chick.mysql.service

import cats.effect.Async
import cats.syntax.functor._
import org.chick.infrastructure.service.IndexService
import org.chick.model.IndexItem
import org.chick.mysql.infrastructure.MySQLIndex

class MySQLService[F[_]](implicit F: Async[F]) extends IndexService[F] {

  private implicit val tableName = "chick001"
  private val index = new MySQLIndex[F]

  override def add(items: Seq[IndexItem]): F[Int] = {
    for {
      items <- index.add(items)
    } yield items.sum
  }

  override def query(q: String): F[Seq[IndexItem]] =
    for {
      items <- index.query(q)
    } yield items

  override def init(): F[Boolean] = {
    for {
      count <- index.createIndexTable(tableName)
    } yield count == 0
  }
}
