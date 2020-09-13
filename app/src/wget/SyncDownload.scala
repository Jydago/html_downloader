package wget

import java.net.UnknownHostException

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import os.{Path, SubPath}

import scala.annotation.tailrec

object SyncDownload {

  def fetchPage(baseUrl: String, subPath: String): (String, Option[Document]) = {
    try {
      (subPath, Some(Jsoup.connect(baseUrl + subPath).get()))
    } catch {
      case _: UnknownHostException => (subPath, None)
    }
  }

  def recursiveSaveHtml(website: String, baseSaveFolder: Path, maxDepth: Int): Set[String] = {
    val fetchSubPage = fetchPage(website, _: String)

    @tailrec
    def recurse(currentSubLinks: Set[String], seenSubLinks: Set[String], currentDepth: Int): Set[String] = {
      if (currentSubLinks.isEmpty || currentDepth >= maxDepth) seenSubLinks
      else {
        println(s"${currentSubLinks.size} links to process.")
        val newSubLinks = currentSubLinks.map(fetchSubPage).flatMap{
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

    val startLink = "/"
    recurse(Set(startLink), Set.empty[String], 0)
  }
}
