package org.chick

import cats.effect._
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.Logger
import java.util.concurrent.{Executors, ScheduledExecutorService}
import mouse.boolean._
import org.chick.infrastructure.service.IndexService
import org.chick.model.IndexItem
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SearchIndexer[F[_]](index: IndexService[F], ref: Ref[F, List[IndexItem]])(implicit F: Async[F]) {

  implicit val timer = new WorkerTimer[F](ExecutionContext.global, Executors.newSingleThreadScheduledExecutor())
  val logger = Logger[SearchIndexer[F]]

  def start(): F[Unit] =
    for {
      _ <- timer.sleep(10.seconds)
      items <- ref.get
      _ <- ref.set(Nil)
      _ <- items.isEmpty.fold(F.unit, F.delay(index.add(items.distinct)))
      _ <- items.isEmpty.fold(F.unit, F.delay(logger.info(s"indexing ${items.length}")))
      _ <- start
    } yield ()
}


final class WorkerTimer[F[_]](ec: ExecutionContext, sc: ScheduledExecutorService)(implicit F: Async[F]) extends Timer[F] {
  override val clock: Clock[F] =
    new Clock[F] {
      override def realTime(unit: TimeUnit): F[Long] =
        F.delay(unit.convert(System.currentTimeMillis(), MILLISECONDS))

      override def monotonic(unit: TimeUnit): F[Long] =
        F.delay(unit.convert(System.nanoTime(), NANOSECONDS))
    }

  override def sleep(timeSpan: FiniteDuration): F[Unit] =
    F.async { cb =>
      val tick = new Runnable {
        override def run() = ec.execute(() => cb(Right(())))
      }
      sc.schedule(tick, timeSpan.length, timeSpan.unit).wait()
    }
}
