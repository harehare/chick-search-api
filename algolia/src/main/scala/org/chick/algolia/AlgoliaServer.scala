package org.chick.algolia

import org.chick.HttpServer
import org.chick.algolia.service.AlgoliaIndexService
import org.chick.infrastructure.ApiEndpoint

object AlgoliaServer
    extends HttpServer(new ApiEndpoint(new AlgoliaIndexService))
