package org.chick.solr

import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

package object infrastructure {

  def liftIO[A](fa: => Future[A])(implicit ec: ExecutionContext): IO[A] =
    IO.async { cb =>
      fa.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }
}
