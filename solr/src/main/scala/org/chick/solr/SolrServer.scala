package org.chick.solr

import org.chick.HttpServer
import org.chick.infrastructure.ApiEndpoint
import org.chick.solr.service.SolrService

object SolrServer extends HttpServer(new ApiEndpoint(new SolrService))
