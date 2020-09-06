import $ivy.`org.jsoup:jsoup:1.13.1`
import org.jsoup._

import scala.jdk.CollectionConverters._
import scala.annotation.tailrec


def findLinks(website: String, subLink: String): Set[String] = {
  val siteHtml = Jsoup.connect(website + subLink).get()
  val hyperlinkTags = siteHtml.select("body a").asScala
  hyperlinkTags.map(_.attr("href")).map{
    case s"$subLink#$_" => subLink
    case subLink => subLink
  }.filter(_.startsWith("/")).toSet
}

def recursiveFindLinks(website: String, depth: Int): Set[String] = {
  val findWebsiteLinks = findLinks(website, _: String)

  @tailrec
  def recurse(currentSubLinks: Set[String], seenSubLinks: Set[String], currentDepth: Int): Set[String] = {
    if (currentSubLinks.isEmpty || currentDepth >= depth) seenSubLinks
    else {
      val newLinks = currentSubLinks.flatMap(findWebsiteLinks)
      recurse(newLinks, seenSubLinks ++ newLinks, currentDepth + 1)
    }
  }

  val startLink = "/"
  recurse(Set(startLink), Set(startLink), 0)
}

@main
def main(uri: String, depth: Int): Unit = {
  recursiveFindLinks(uri, 2).foreach(println)
}




