package downloader

import os.{Path, SubPath}

import scala.annotation.tailrec

object SyncDownload {

  def recursiveSaveHtml(extractor: ExtractUtilsTrait, website: String, baseSaveFolder: Path, maxDepth: Int): Set[String] = {
    val fetchSubPage = extractor.fetchPage(website, _: String)

    @tailrec
    def recurse(currentSubLinks: Set[String], seenSubLinks: Set[String], currentDepth: Int): Set[String] = {
      if (currentSubLinks.isEmpty || (currentDepth >= maxDepth && maxDepth != -1)) seenSubLinks
      else {
        println(s"${currentSubLinks.size} links to process and download.")
        val newSubLinks = currentSubLinks.map(fetchSubPage).flatMap{
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

    val startLink = "/"
    recurse(Set(startLink), Set.empty[String], 0)
  }
}
