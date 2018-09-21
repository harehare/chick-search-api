package org.chick.mysql.infrastructure

import cats.effect.Async
import cats.implicits._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import java.time.{LocalDate, ZoneId}
import org.chick.model.{IndexItem, ItemType}

class MySQLIndex[F[_]](implicit F: Async[F]) {

  val xa = Transactor.fromDriverManager[F](
    "com.mysql.jdbc.Driver",
    sys.env("MYSQL_URI"),
    sys.env("MYSQL_USER"),
    sys.env("MYSQL_PASSWORD")
  )

  implicit val itemTypeMeta: Meta[ItemType] =
    Meta[String].xmap(ItemType.apply, _.name)

  def add(items: Seq[IndexItem])(implicit tableName: String): F[Seq[Int]] =
    for {
      count <- items.map(insertIndexItem(tableName, _)).toList.sequence
    } yield count

  def createIndexTable(tableName: String): F[Int] =
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

  private def insertIndexItem(tableName: String, item: IndexItem): F[Int] =
    (fr"INSERT INTO " ++ Fragment.const(tableName) ++
      fr"""(
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

  def query(q: String)(implicit tableName: String): F[List[IndexItem]] =
    for {
      items <- selectIndexItem(tableName, q)
      res <- F.delay{
        items.map(
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
      }
    } yield (res)


  def selectIndexItem(tableName: String, q: String)
  : F[List[(String, String, String, ItemType, String, LocalDate)]] =
    (fr"SELECT title, url, body, itemType, tags, createdAt FROM " ++ Fragment
      .const(tableName) ++ fr" WHERE MATCH (title, body, tags) AGAINST ($q IN BOOLEAN MODE)")
      .query[(String, String, String, ItemType, String, LocalDate)]
      .to[List]
      .transact(xa)
}
