package downloader

import java.net.UnknownHostException

import org.asynchttpclient.AsyncHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import os.{Path, SubPath}

import scala.concurrent.{ExecutionException, Future, Promise}
import scala.jdk.CollectionConverters._

trait ExtractUtilsTrait {

  def fetchPage(baseUrl: String, subPath: String): (String, Option[Document])
  def fetchPageAsync(baseUrl: String, subPath: String, client: AsyncHttpClient): Future[(String, Option[Document])]
  def extractLinks(siteHtml: Document): Set[String]
  def handlePath(path: Path, subPath: SubPath): Path
  def saveHtml(fullFilePath: Path, htmlDoc: Document): Unit
}


object ExtractUtils extends ExtractUtilsTrait {

  def fetchPage(baseUrl: String, subPath: String): (String, Option[Document]) = {
    try {
      (subPath, Some(Jsoup.connect(baseUrl + subPath).get()))
    } catch {
      case _: UnknownHostException => (subPath, None)
    }
  }

  def fetchPageAsync(baseUrl: String, subPath: String, client: AsyncHttpClient): Future[(String, Option[Document])] = {
    val p = Promise[(String, Option[Document])]
    val listenableF = client.prepareGet(baseUrl + subPath).execute()
    listenableF.addListener(() => {
      try {
        p.success((subPath, Some(Jsoup.parse(listenableF.get().getResponseBody))))
      } catch {
        case _: ExecutionException => println(s"Crashed on $subPath"); p.success((subPath, None))
      }
    }, null)
    p.future
  }

  def extractLinks(siteHtml: Document): Set[String] = {
    val hyperlinkTags = siteHtml.select("body a").asScala
    hyperlinkTags.map(_.attr("href")).map{
      case s"$subLink#$_" => subLink // Some links use # parameters to move to a certain section of a web page
      case subLink => subLink
    }.filter(_ != "") // <a> tags with no href will return empty string
      .filter(_.startsWith("/")) // Only take links that refer to local file hierarchy on website
      .filter(!_.contains("www")) // Remove random links that happen to start with / but are actually links to other
      // sites
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
