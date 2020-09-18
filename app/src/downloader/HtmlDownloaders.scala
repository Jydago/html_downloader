package downloader

import java.net.UnknownHostException

import downloader.HtmlDocumentHandling.SubLink
import org.asynchttpclient.AsyncHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionException, Future, Promise}

object HtmlDownloaders {

  def downloadPageAsync(uri: String, subLink: SubLink, client: AsyncHttpClient): Future[(SubLink, Option[Document])] = {
    val p = Promise[(SubLink, Option[Document])]
    val listenableF = client.prepareGet(uri + subLink).execute()
    listenableF.addListener(() => {
      try p.success((subLink, Some(Jsoup.parse(listenableF.get().getResponseBody))))
      catch {
        case _: ExecutionException => println(s"Crashed on $subLink"); p.success((subLink, None))
      }
    }, null)
    p.future
  }

  def downloadPageSync(uri: String, subLink: SubLink): Future[(String, Option[Document])] = {
    try {
      Future.successful(subLink, Some(Jsoup.connect(uri + subLink).get()))
    } catch {
      case _: UnknownHostException => Future.successful(subLink, None)
    }
  }

}
