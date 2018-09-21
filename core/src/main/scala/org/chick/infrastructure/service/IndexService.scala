package org.chick.infrastructure.service

import org.chick.model.IndexItem

trait IndexService[F[_]] {

  def init(): F[Boolean]

  def add(items: Seq[IndexItem]): F[Int]

  def query(q: String): F[Seq[IndexItem]]

}
