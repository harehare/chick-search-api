package org.chick

import cats.Show
import org.chick.model.ItemType

object JsonImplicits {
  import io.circe._
  implicit val itemTypeEncode: Encoder[ItemType] =
    Encoder[String].contramap(_.name)

  implicit val itemTypeDecode: Decoder[ItemType] =
    Decoder[String].map(ItemType(_))
}

object ShowImplicits {
  implicit val itemTypeShow: Show[ItemType] = Show.show[ItemType] {
    case ItemType.Bookmark => "bookmark"
    case ItemType.History  => "history"
    case ItemType.Pocket   => "pocket"
  }
}
