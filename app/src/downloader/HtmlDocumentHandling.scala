package downloader

import org.jsoup.nodes.Document
import os.{Path, SubPath}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

object HtmlDocumentHandling {

  type SubLink = String // A string that should always start with a /
  type DownloadResults = (SubLink, Option[Document])

  def recurseDownloadSaveHtml(downloadSubPage: SubLink => Future[DownloadResults],
                              saveHtml: (SubLink, Document) => Unit,
                              extractLinks: Document => Set[SubLink],
                              maxDepth: Int)
                             (implicit ec: ExecutionContext): Future[Set[SubLink]] = {

    def recurse(currentSubLinks: Set[SubLink], seenSubLinks: Set[SubLink], currentDepth: Int): Future[Set[SubLink]] = {
      if (currentSubLinks.isEmpty || (currentDepth >= maxDepth && maxDepth != -1)) Future.successful(seenSubLinks)
      else {
        println(s"${currentSubLinks.size} pages to process and download.")
        Future.sequence(currentSubLinks.map(downloadSubPage)).flatMap{ downloadResults =>
          val newSubLinks = downloadResults.flatMap{
            case (subLink, Some(htmlDoc)) =>
              saveHtml(subLink, htmlDoc)
              extractLinks(htmlDoc)
            case (_, None) => Set.empty[SubLink]
          }
          val processedSubLinks = seenSubLinks ++ currentSubLinks
          recurse(newSubLinks.filter(!processedSubLinks.contains(_)), processedSubLinks, currentDepth + 1)
        }
      }
    }

    val startSubLink = "/"
    recurse(Set(startSubLink), Set.empty[SubLink], 0)
  }

  def extractLinks(siteHtml: Document): Set[SubLink] = {
    val hyperlinkTags = siteHtml.select("body a").asScala
    hyperlinkTags.map(_.attr("href")).map{
      case s"$subLink#$_" => subLink // Some links use # parameters to move to a certain section of a web page
      case subLink => subLink
    }.filter(_ != "") // <a> tags with no href will return empty string
      .filter(_.startsWith("/")) // Only take links that refer to local file hierarchy on website
      .filter(!_.contains("www")) // Remove links that happen to start with / but are actually links to other sites
      .toSet
  }

  def transformHtmlLinkToFolderPath(path: Path, subLink: SubLink): Path = {
    val subPath = SubPath(subLink.dropWhile(_ == '/'))
    if (subPath == SubPath("")) path / "index.html"
    else if (subPath.last.endsWith(".html")) path / subPath
    else path / subPath / os.up / s"${subPath.last}.html"
  }

  def saveHtml(fullFilePath: Path, htmlDoc: Document): Unit = {
    os.makeDir.all(fullFilePath / os.up) // Up to avoid creating file as dir
    os.write(fullFilePath, htmlDoc.toString)
  }

}
