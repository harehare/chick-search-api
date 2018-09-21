package org.chick.mysql

import cats.effect.IO
import org.chick.HttpServer
import org.chick.mysql.service.MySQLService

object MySQLServer extends HttpServer[IO](new MySQLService[IO])
