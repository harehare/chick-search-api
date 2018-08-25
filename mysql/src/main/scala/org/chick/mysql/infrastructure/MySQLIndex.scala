package org.chick.mysql.infrastructure

import java.time.{LocalDate, ZoneId}

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import org.chick.model.{IndexItem, ItemType}

import scala.concurrent.ExecutionContext.Implicits.global

object MySQLIndex {

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.jdbc.Driver",
    sys.env("MYSQL_URI"),
    sys.env("MYSQL_USER"),
    sys.env("MYSQL_PASSWORD")
  )

  implicit val itemTypeMeta: Meta[ItemType] =
    Meta[String].xmap(ItemType.apply, _.name)

  def add(items: Seq[IndexItem])(implicit tableName: String): IO[Seq[Int]] =
    for {
      count <- items.map(insertIndexItem(tableName, _)).toList.sequence
    } yield count

  def createIndexTable(tableName: String): IO[Int] =
    (fr"CREATE TABLE IF NOT EXISTS " ++ Fragment.const(tableName) ++
      fr"""(
      id INT(11) NOT NULL AUTO_INCREMENT,
      title VARCHAR(64) NULL DEFAULT NULL,
      url VARCHAR(64) NULL DEFAULT NULL,
      body TEXT NULL,
      itemType VARCHAR(64) NULL,
      tags TEXT NULL,
      createdAt DATETIME NULL,
      PRIMARY KEY (id),
      FULLTEXT INDEX `bigram_columns` (title, body, tags)
    )
    """).update.run.transact(xa)

  private def insertIndexItem(tableName: String, item: IndexItem): IO[Int] =
    (fr"INSERT INTO " ++ Fragment.const(tableName) ++ fr"""(
           title,
           url,
           body,
           itemType,
           tags,
           createdAt
         ) VALUES (
           ${item.title},
           ${item.url},
           ${item.body},
           ${item.itemType.name},
           ${item.tags.mkString(",")},
           now()
         )
         """).update.run.transact(xa)

  def query(q: String)(implicit tableName: String): IO[List[IndexItem]] =
    for {
      indexItems <- selectIndexItem(tableName, q)
      items <- IO(
        indexItems.map(
          item =>
            IndexItem(item._1,
                      item._2,
                      item._3,
                      item._4,
                      Some(item._5.split(",").toList),
                      Some(
                        item._6.atStartOfDay
                          .atZone(ZoneId.systemDefault)
                          .toInstant
                          .toEpochMilli)))
      )
    } yield items

  def selectIndexItem(tableName: String, q: String)
    : IO[List[(String, String, String, ItemType, String, LocalDate)]] =
    (fr"SELECT title, url, body, itemType, tags, createdAt FROM " ++ Fragment
      .const(tableName) ++ fr" WHERE MATCH (title, body, tags) AGAINST ($q IN BOOLEAN MODE)")
      .query[(String, String, String, ItemType, String, LocalDate)]
      .to[List]
      .transact(xa)
}
