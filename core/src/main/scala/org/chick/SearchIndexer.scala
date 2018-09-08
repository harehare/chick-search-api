package org.chick

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.Logger
import mouse.boolean._
import org.chick.infrastructure.service.IndexService
import org.chick.model.IndexItem

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SearchIndexer(index: IndexService, ref: Ref[IO, List[IndexItem]]) {

  implicit val timer = IO.timer(ExecutionContext.global)
  val logger = Logger[SearchIndexer]

  def start(): IO[Unit] =
    for {
      _ <- IO.sleep(10.seconds)
      items <- ref.get
      _ <- ref.set(Nil)
      _ <- IO.pure(
        (items.isEmpty)
          .fold(IO.unit, index.add(items.distinct)))
      _ <- IO(
        (items.isEmpty).fold((), logger.info(s"indexing ${items.length}")))
      _ <- start
    } yield ()
}
