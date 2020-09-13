package wget

import org.asynchttpclient.AsyncHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import os.{Path, SubPath}

import scala.concurrent.{ExecutionContext, ExecutionException, Future, Promise}

object AsyncDownload {
  val client: AsyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient()

  def fetchPageAsync(baseUrl: String, subPath: String, client: AsyncHttpClient): Future[(String, Option[Document])] = {
    val p = Promise[(String, Option[Document])]
    val listenableF = client.prepareGet(baseUrl + subPath).execute()
    listenableF.addListener(() => {
      try {
        p.success((subPath, Some(Jsoup.parse(listenableF.get().getResponseBody))))
      } catch {
        case b: ExecutionException => println(s"Crashed on $subPath"); p.success((subPath, None))
      }
    }, null)
    p.future
  }

  def recurseSaveHtmlAsync(website: String, baseSaveFolder: Path, maxDepth: Int)(implicit ec: ExecutionContext): Future[Set[String]] = {
    val fetchSubPageAsync = fetchPageAsync(website, _: String, client)

    def recurse(currentSubLinks: Set[String], seenSubLinks: Set[String], currentDepth: Int): Future[Set[String]] = {
      if (currentSubLinks.isEmpty || currentDepth >= maxDepth) Future.successful(seenSubLinks)
      else {
        println(s"${currentSubLinks.size} pages to process and download.")
        Future.sequence(currentSubLinks.map(fetchSubPageAsync)).flatMap{ fetchResults =>
          val newSubLinks = fetchResults.flatMap{
            case (subPath, Some(htmlDoc)) =>
              val fullFilePath = Functions.handlePath(baseSaveFolder, SubPath(subPath.dropWhile(_ == '/')))
              Functions.saveHtml(fullFilePath, htmlDoc)
              Functions.extractLinks(htmlDoc)
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
