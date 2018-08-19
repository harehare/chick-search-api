package org.chick

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.chick.infrastructure.ApiEndpoint
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

abstract class HttpServer(endpoint: ApiEndpoint) extends StreamApp[IO] {
  override def stream(args: List[String],
                      requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(sys.env("PORT").toInt, "0.0.0.0")
      .mountService(endpoint.service, "/")
      .serve
}
