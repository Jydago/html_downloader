package wget

object Main extends App {

  def runSyncDownload(uri: String, basePath: os.Path, maxDepth: Int): Set[String] = {
    SyncDownload.recursiveSaveHtml(uri, basePath, maxDepth)
  }

  def runAsyncDownload(uri: String, basePath: os.Path, maxDepth: Int): Set[String] = {
    import scala.concurrent.duration.Duration
    import scala.concurrent.{Await, ExecutionContext}

    implicit val ec: ExecutionContext = ExecutionContext.global
    val allSubLinksF = AsyncDownload.recurseSaveHtmlAsync(uri, basePath, maxDepth)
    val allSubLinks = Await.result(allSubLinksF, Duration.Inf).toSeq.sorted
    println(s"A total of ${allSubLinks.length} pages were downloaded")
    Await.result(allSubLinksF, Duration.Inf)
  }

  val uri = args(1)
  val basePath = os.pwd / "download_output"
  if (os.exists(basePath)) os.remove.all(basePath)
  val maxDepth = args(2).toInt

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
