package org.chick.mysql.service

import cats.effect.IO
import org.chick.infrastructure.service.IndexService
import org.chick.model.IndexItem
import org.chick.mysql.infrastructure.MySQLIndex

object MySQLService extends IndexService {

  private implicit val tableName = "chick001"

  override def add(items: Seq[IndexItem]): IO[Int] = {
    for {
      items <- MySQLIndex.add(items)
    } yield items.sum
  }

  override def query(q: String): IO[Seq[IndexItem]] =
    for {
      items <- MySQLIndex.query(q)
    } yield items

  override def init(): IO[Option[Unit]] = {
    for {
      _ <- MySQLIndex.createIndexTable(tableName)
    } yield None
  }
}
