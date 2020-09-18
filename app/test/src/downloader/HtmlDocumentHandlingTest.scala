package downloader

import downloader.HtmlDocumentHandling.{DownloadResults, SubLink}
import org.jsoup.nodes.Document
import os.Path
import utest.TestSuite
import utest._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object HtmlDocumentHandlingTest extends TestSuite {

  val tests: Tests = Tests {

    test("recursiveDownloadSaveHtml") {
      implicit val ec: ExecutionContext = ExecutionContext.global
      val maxDepth = 6
      val mockSaveHtml = (_: SubLink, _: Document) => ()

      val generatedLinkMapping = TestUtils.generateHtmlTree(Set("/"), Map.empty[SubLink, Document], 0, maxDepth)
      val generatedLinks = generatedLinkMapping.keys.toSet
      val mockDownloaderMap: Map[SubLink, Future[DownloadResults]] = generatedLinkMapping.map{
        case (subLink, htmlDoc) => (subLink, Future.successful((subLink, Some(htmlDoc))))
      }
      val foundLinksF = HtmlDocumentHandling.recurseDownloadSaveHtml(mockDownloaderMap.apply, mockSaveHtml, HtmlDocumentHandling.extractLinks, -1)
      val foundLinks = Await.result(foundLinksF, Duration.Inf)
      assert(foundLinks.nonEmpty)
      // Easier to debug and parse error info with diff instead of == since there can be quite a few links
      assert(foundLinks.diff(generatedLinks).isEmpty)
    }


    test("transformHtmlPathToFolderPath") {
      case class TestDataResult(input: String, expected: String)
      val testDataResults = Seq(
        TestDataResult("", "index.html"),
        TestDataResult("file.html", "file.html"),
        TestDataResult("file", "file.html"),
        TestDataResult("some/path/file.html", "some/path/file.html"),
        TestDataResult("some/path/file", "some/path/file.html"),
        TestDataResult("some/path/file/", "some/path/file.html")
      )
      val basePath = Path("/")
      for (testData <- testDataResults) {
        val fixedPath = HtmlDocumentHandling.transformHtmlLinkToFolderPath(basePath, testData.input)
        assert(fixedPath == Path(s"/${testData.expected}"))
      }
    }

    test("extractLink") {
      val testData = TestUtils.generateHtmlDocument(10, 20)
      val extractedLinks = HtmlDocumentHandling.extractLinks(testData._1)
      assert(extractedLinks == testData._2)
    }

  }

}


