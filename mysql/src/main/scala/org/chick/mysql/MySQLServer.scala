package org.chick.mysql

import org.chick.HttpServer
import org.chick.infrastructure.ApiEndpoint
import org.chick.mysql.service.MySQLService

object MySQLServer extends HttpServer(new ApiEndpoint(new MySQLService))
