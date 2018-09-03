package org.chick.algolia

import org.chick.HttpServer
import org.chick.algolia.service.AlgoliaIndexService

object AlgoliaServer extends HttpServer(AlgoliaIndexService)
