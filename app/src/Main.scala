
import org.jsoup.nodes.Document

import scala.util.Try
import downloader.HtmlDocumentHandling._
import downloader.HtmlDownloaders._

import scala.concurrent._
import scala.concurrent.duration.Duration

object Main extends App {

  val uri = args(1)
  val maxDepth = Try(args(2).toInt).getOrElse(-1)
  val basePath = os.pwd / "download_output"
  if (os.exists(basePath)) os.remove.all(basePath)

  implicit val ec: ExecutionContext = ExecutionContext.global

  val saveHtmlToPath = (subLink: SubLink, htmlDoc: Document) => {
    val fullFilePath = transformHtmlLinkToFolderPath(basePath, subLink)
    saveHtml(fullFilePath, htmlDoc)
  }

  val allSubLinks = args(0) match {
    case "sync" =>
      val downloadSubPage = downloadPageSync(uri, _: SubLink)
      val linksF = recurseDownloadSaveHtml(downloadSubPage, saveHtmlToPath, extractLinks, maxDepth)
      Await.result(linksF, Duration.Inf)
    case "async" =>
      val client = org.asynchttpclient.Dsl.asyncHttpClient()
      try {
        val downloadSubPage = downloadPageAsync(uri, _: SubLink, client)
        val linksF = recurseDownloadSaveHtml(downloadSubPage, saveHtmlToPath, extractLinks, maxDepth)
        Await.result(linksF, Duration.Inf)
      } finally client.close()
    case _ =>
      println("First argument has to be either `sync` or `async`.")
      Set.empty[String]
  }

  println(s"A total of ${allSubLinks.size} pages were downloaded.")
  System.exit(0)
}
