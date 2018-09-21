package org.chick.elasticsearch

import cats.effect.IO
import org.chick.HttpServer
import org.chick.elasticsearch.service.ElasticsarchService

object ElasticsearchServer extends HttpServer[IO](new ElasticsarchService[IO])
