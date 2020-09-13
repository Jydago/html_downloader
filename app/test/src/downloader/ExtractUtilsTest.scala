package downloader

import org.scalatest.funsuite.AnyFunSuite
import os.{Path, SubPath}

class ExtractUtilsTest extends AnyFunSuite {

  case class TestDataResult(input: String, expected: String)

  test("handlePath should return correct path"){
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
      val fixedPath = ExtractUtils.handlePath(basePath, SubPath(testData.input))
      assert(fixedPath == Path(s"/${testData.expected}"))
    }
  }

  test("extractLink should find all links"){
    val testData = TestUtils.generateHtmlDocument(10, 20)
    val extractedLinks = ExtractUtils.extractLinks(testData._1)
//    assert((extractedLinks diff testData._2).isEmpty)
    assert(extractedLinks == testData._2)
  }


}
