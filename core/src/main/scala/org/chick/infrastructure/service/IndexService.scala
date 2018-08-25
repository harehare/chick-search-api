package org.chick.infrastructure.service

import cats.effect.IO
import org.chick.model.IndexItem

trait IndexService {

  def init(): IO[Option[_]]

  def add(items: Seq[IndexItem]): IO[Int]

  def query(q: String): IO[Seq[IndexItem]]
}
