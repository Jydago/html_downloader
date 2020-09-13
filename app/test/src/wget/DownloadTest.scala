package wget

import org.asynchttpclient.AsyncHttpClient
import org.jsoup.nodes.Document
import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory
import org.jsoup._

import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.Random

class DownloadTest extends AnyFunSuite with MockFactory {
  implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def initiateCommonExtractUtilsMock(mockExtractor: ExtractUtilsTrait): ExtractUtilsTrait = {
    (mockExtractor.handlePath _).expects(*, *)
      .onCall((path, subPath) => ExtractUtils.handlePath(path, subPath)).anyNumberOfTimes()
    (mockExtractor.extractLinks _).expects(*)
      .onCall{ htmlDoc: Document => ExtractUtils.extractLinks(htmlDoc) }.anyNumberOfTimes()
    (mockExtractor.saveHtml _).expects(*, *).returns().anyNumberOfTimes()
    mockExtractor
  }

  test("sync"){
    val mockExtractor = initiateCommonExtractUtilsMock(mock[ExtractUtilsTrait])
    val maxDepth = 4
    val baseUrl = "https://foo.bar"
    val emptyHtmlDoc = Jsoup.parse("")

    @tailrec
    def generateHtmlTree(newLinks: Set[String], totalLinks: Set[String], currentDepth: Int): Set[String] = {
      if (currentDepth >= maxDepth) {
        newLinks.foreach(link =>
          (mockExtractor.fetchPage _)
            .expects(baseUrl, link)
            .returns((link, Some(emptyHtmlDoc))
            )
        )
        newLinks ++ totalLinks
      } else {
        val generatedLinks = newLinks.flatMap{ link =>
          val nbrLinks = Random.nextInt(5) + 1
          val (htmlDoc, myLinks) = TestUtils.generateHtmlDocument(nbrLinks, 20)
          (mockExtractor.fetchPage _)
            .expects(baseUrl, link)
            .returns((link, Some(htmlDoc))
            )
          myLinks
        }
        generateHtmlTree(generatedLinks, totalLinks ++ newLinks, currentDepth + 1)
      }
    }

    val generatedLinks = generateHtmlTree(Set("/"), Set.empty[String], 0)

    val foundLinks = SyncDownload.recursiveSaveHtml(mockExtractor, baseUrl, os.pwd / "test_output", -1)
    assert(foundLinks.nonEmpty)
    assert(foundLinks == generatedLinks)
  }

  test("async") {
    val mockExtractor = initiateCommonExtractUtilsMock(mock[ExtractUtilsTrait])
    val maxDepth = 4
    val baseUrl = "https://foo.bar"
    val emptyHtmlDoc = Jsoup.parse("")
    val mockAsyncClient = mock[AsyncHttpClient]


    @tailrec
    def generateHtmlTree(newLinks: Set[String], totalLinks: Set[String], currentDepth: Int): Set[String] = {
      if (currentDepth >= maxDepth) {
        newLinks.foreach(link =>
          (mockExtractor.fetchPageAsync _)
            .expects(baseUrl, link, mockAsyncClient)
            .returns(Future.successful((link, Some(emptyHtmlDoc))))
        )
        newLinks ++ totalLinks
      } else {
        val generatedLinks = newLinks.flatMap{ link =>
          val nbrLinks = Random.nextInt(5) + 1
          val (htmlDoc, myLinks) = TestUtils.generateHtmlDocument(nbrLinks, 20)
          (mockExtractor.fetchPageAsync _)
            .expects(baseUrl, link, mockAsyncClient)
            .returns(Future.successful((link, Some(htmlDoc))))
          myLinks
        }
        generateHtmlTree(generatedLinks, totalLinks ++ newLinks, currentDepth + 1)
      }
    }

    val generatedLinks = generateHtmlTree(Set("/"), Set.empty[String], 0)

    val foundLinksF = AsyncDownload.recurseSaveHtmlAsync(mockExtractor, baseUrl, os.pwd / "test_output", -1, mockAsyncClient)
    val foundLinks = Await.result(foundLinksF, Duration.Inf)
    assert(foundLinks.nonEmpty)
    assert(foundLinks == generatedLinks)
  }



}
