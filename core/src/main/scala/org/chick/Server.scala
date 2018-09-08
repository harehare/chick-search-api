package org.chick

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.chick.infrastructure.ApiEndpoint
import org.chick.infrastructure.service.IndexService
import org.chick.model.IndexItem
import org.http4s.server.blaze.BlazeBuilder

abstract class HttpServer(val service: IndexService) extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      ref <- Ref.of[IO, List[IndexItem]](Nil)
      endpoint <- IO(new ApiEndpoint(service, ref))
      _ <- List(
        new SearchIndexer(service, ref).start,
        BlazeBuilder[IO]
          .withNio2(true)
          .bindHttp(sys.env("PORT").toInt, "0.0.0.0")
          .mountService(endpoint.service, "/")
          .start
      ).parSequence.void
    } yield ExitCode.Success

}
