package org.chick.algolia

import cats.effect.IO
import org.chick.HttpServer
import org.chick.algolia.service.AlgoliaService

object AlgoliaServer extends HttpServer[IO](new AlgoliaService[IO])
