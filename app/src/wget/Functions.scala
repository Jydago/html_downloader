package wget

import org.jsoup.nodes.Document
import os.{Path, SubPath}
import scala.jdk.CollectionConverters._

object Functions {

  def extractLinks(siteHtml: Document): Set[String] = {
    val hyperlinkTags = siteHtml.select("body a").asScala
    hyperlinkTags.map(_.attr("href")).map{
      case s"$subLink#$_" => subLink // Some links use # parameters to move to a certain section of a web page
      case subLink => subLink
    }.filter(_.startsWith("/")) // Only take links that refer to local file hierarchy on website
      .filter(!_.contains("www")) // Remove random links that happen to start with / but are actually links to other
      .toSet
  }

  def handlePath(path: Path, subPath: SubPath): Path = {
    if (subPath == SubPath("")) path / "index.html"
    else if (subPath.last.endsWith(".html")) path / subPath
    else path / subPath / os.up / s"${subPath.last}.html"
  }

  def saveHtml(fullFilePath: Path, htmlDoc: Document): Unit = {
    os.makeDir.all(fullFilePath / os.up) // Up to avoid creating file as dir
    os.write(fullFilePath, htmlDoc.toString)
  }

}
