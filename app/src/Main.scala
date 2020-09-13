import downloader.ExtractUtils

import scala.util.Try

object Main extends App {

  def runSyncDownload(uri: String, basePath: os.Path, maxDepth: Int): Set[String] = {
    import downloader.SyncDownload
    SyncDownload.recursiveSaveHtml(ExtractUtils, uri, basePath, maxDepth)
  }

  def runAsyncDownload(uri: String, basePath: os.Path, maxDepth: Int): Set[String] = {
    import org.asynchttpclient.Dsl.asyncHttpClient
    import downloader.AsyncDownload

    import scala.concurrent.duration.Duration
    import scala.concurrent.{Await, ExecutionContext}

    implicit val ex: ExecutionContext = ExecutionContext.global // ExecutionContext.global can handle `blocking{}` call inside Futures
    // FixedThreadPool doesn't support `blocking{}`
    //    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
    val client = asyncHttpClient()
    val allSubLinksF = AsyncDownload.recurseSaveHtmlAsync(ExtractUtils, uri, basePath, maxDepth, client)
    Await.result(allSubLinksF, Duration.Inf)
  }

  val uri = args(1)
  val basePath = os.pwd / "download_output"
  if (os.exists(basePath)) os.remove.all(basePath)
  val maxDepth = Try(args(2).toInt).getOrElse(-1)

  val allSubLinks = args(0) match {
    case "sync" => runSyncDownload(uri, basePath, maxDepth)
    case "async" => runAsyncDownload(uri, basePath, maxDepth)
    case _ =>
      println("First argument has to be either `sync` or `async`.")
      Set.empty[String]
  }

  println(s"A total of ${allSubLinks.size} pages were downloaded")
  System.exit(0)
}
