package org.chick.solr

import cats.effect.IO
import org.chick.HttpServer
import org.chick.solr.service.SolrService

object SolrServer extends HttpServer[IO](new SolrService[IO])
