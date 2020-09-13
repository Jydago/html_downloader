package wget

import org.asynchttpclient.AsyncHttpClient
import os.{Path, SubPath}

import scala.concurrent.{ExecutionContext, Future}

object AsyncDownload {

  def recurseSaveHtmlAsync(extractor: ExtractUtilsTrait, website: String, baseSaveFolder: Path, maxDepth: Int, client: AsyncHttpClient)
                          (implicit ec: ExecutionContext): Future[Set[String]] = {
    val fetchSubPageAsync = extractor.fetchPageAsync(website, _: String, client)

    def recurse(currentSubLinks: Set[String], seenSubLinks: Set[String], currentDepth: Int): Future[Set[String]] = {
      if (currentSubLinks.isEmpty || (currentDepth >= maxDepth && maxDepth != -1)) Future.successful(seenSubLinks)
      else {
        println(s"${currentSubLinks.size} pages to process and download asynchronously.")
        Future.sequence(currentSubLinks.map(fetchSubPageAsync)).flatMap{ fetchResults =>
          val newSubLinks = fetchResults.flatMap{
            case (subPath, Some(htmlDoc)) =>
              val fullFilePath = extractor.handlePath(baseSaveFolder, SubPath(subPath.dropWhile(_ == '/')))
              extractor.saveHtml(fullFilePath, htmlDoc)
              extractor.extractLinks(htmlDoc)
            case (_, None) => Set.empty[String]
          }
          val processedSubLinks = seenSubLinks ++ currentSubLinks
          recurse(newSubLinks.filter(!processedSubLinks.contains(_)), processedSubLinks, currentDepth + 1)
        }
      }
    }

    val startSubLink = "/"
    recurse(Set(startSubLink), Set(startSubLink), 0)
  }
}
