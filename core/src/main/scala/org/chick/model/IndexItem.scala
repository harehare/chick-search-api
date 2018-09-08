package org.chick.model

case class IndexItem(
    title: String,
    url: String,
    body: String,
    itemType: ItemType,
    tags: Option[List[String]],
    createdAt: Option[Long],
)

sealed abstract class ItemType(val name: String)

object ItemType extends (String => ItemType) {
  case object Bookmark extends ItemType("bookmark")
  case object History extends ItemType("history")
  case object Pocket extends ItemType("pocket")

  def apply(itemType: String): ItemType = itemType match {
    case "bookmark" => Bookmark
    case "history"  => History
    case "pocket"   => Pocket
    case _          => History
  }
}
