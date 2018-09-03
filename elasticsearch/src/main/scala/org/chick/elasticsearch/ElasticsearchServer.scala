package org.chick.elasticsearch

import org.chick.HttpServer
import org.chick.elasticsearch.service.ElasticsarchService

object ElasticsearchServer extends HttpServer(ElasticsarchService)
