package org.chick.model

case class IndexResponse(
    count: Int
)

case class SearchResponse(
    title: String,
    url: String,
    snippet: String,
    itemType: ItemType,
    tags: Option[List[String]],
    bookmark: Boolean
)
