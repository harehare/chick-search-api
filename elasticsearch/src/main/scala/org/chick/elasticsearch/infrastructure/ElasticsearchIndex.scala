package org.chick.elasticsearch.infrastructure

import cats.effect.IO
import mouse.boolean._
import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.analyzers.{
  CustomAnalyzerDefinition,
  LowercaseTokenFilter,
  NGramTokenizer,
  UniqueTokenFilter
}
import com.sksamuel.elastic4s.http.bulk.BulkResponse
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.index.admin.AliasActionResponse
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import mouse.option._
import org.chick.model.IndexItem

object ElasticsearchIndex {
  import com.sksamuel.elastic4s.http.ElasticDsl._

  val client = ElasticClient(ElasticProperties(sys.env("ES_URL")))

  def create(version: String)(
      implicit indexName: String): IO[CreateIndexResponse] =
    for {
      mappingResult <- IO(
        client.execute { getMapping(indexName / "document") }.await)
      response <- IO(
        (mappingResult.isError || (mappingResult.isSuccess && mappingResult.result.isEmpty))
          .fold(
            client
              .execute {
                createIndex(version)
                  .mappings(
                    mapping("document").as(
                      textField("title") analyzer "chick_analyzer" boost 10.0,
                      textField("body") analyzer "chick_analyzer",
                      keywordField("itemType"),
                      keywordField("tags"),
                      longField("createdAt")
                    )
                  )
                  .analysis(
                    CustomAnalyzerDefinition(
                      "chick_analyzer",
                      NGramTokenizer("chick_ngram_tokenizer",
                                     2,
                                     3,
                                     Seq("letter", "digit")),
                      LowercaseTokenFilter,
                      UniqueTokenFilter
                    )
                  ) alias indexName
              }
              .await
              .result,
            CreateIndexResponse(false, false)
          )
      )
    } yield response

  def switchAliases(oldVersion: String, newVersion: String)(
      implicit indexName: String): IO[AliasActionResponse] =
    IO {
      client
        .execute {
          aliases(
            removeAlias(indexName) on oldVersion,
            addAlias(indexName) on newVersion
          )
        }
        .await
        .result
    }

  def add(items: Seq[IndexItem])(implicit indexName: String): IO[BulkResponse] =
    IO {
      client
        .execute {
          bulk(
            items.map(x => {
              indexInto(indexName / "document").fields(
                "title" -> x.title,
                "url" -> x.url,
                "body" -> x.body,
                "itemType" -> x.itemType.name,
                "tags" -> x.tags.cata(_ => x.tags.get, Nil),
                "createdAt" -> System.currentTimeMillis)
            })
          ).refresh(RefreshPolicy.WaitFor)
        }
        .await
        .result
    }

  def query(q: String)(implicit indexName: String): IO[SearchResponse] =
    IO {
      client
        .execute {
          search(indexName).query {
            boolQuery.should(
              matchQuery("title", q),
              matchQuery("body", q)
            )
          }
        }
        .await
        .result
    }
}
