package org.chick.mysql

import org.chick.HttpServer
import org.chick.mysql.service.MySQLService

object MySQLServer extends HttpServer(MySQLService)
