package org.chick

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import org.chick.infrastructure.ApiEndpoint
import org.chick.infrastructure.service.IndexService
import org.chick.model.IndexItem
import org.http4s.server.blaze.BlazeBuilder

abstract class HttpServer[F[_]](val service: IndexService[IO]) extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
      for {
        ref <- Ref.of[IO, List[IndexItem]](Nil)
        endpoint = new ApiEndpoint[IO](service, ref)
        _ <- List(
          new SearchIndexer[IO](service, ref).start,
          BlazeBuilder[IO]
            .bindHttp(8080)
            .mountService(endpoint.service, "/")
            .start
        ).parSequence.void
      } yield ExitCode.Success
}

