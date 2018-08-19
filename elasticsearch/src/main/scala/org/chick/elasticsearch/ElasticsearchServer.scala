package org.chick.elasticsearch

import org.chick.HttpServer
import org.chick.elasticsearch.service.ElasticsarchService
import org.chick.infrastructure.ApiEndpoint

object ElasticsearchServer
    extends HttpServer(new ApiEndpoint(new ElasticsarchService))
