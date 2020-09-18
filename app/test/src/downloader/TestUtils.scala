package downloader

import downloader.HtmlDocumentHandling.SubLink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.annotation.tailrec
import scala.util.Random

object TestUtils {

  @tailrec
  def generateHtmlTree(newLinks: Set[SubLink], linkMapping: Map[SubLink, Document], currentDepth: Int, maxDepth: Int): Map[SubLink, Document] = {
    if (currentDepth >= maxDepth) {
      val emptyHtmlDoc = Jsoup.parse("")
      linkMapping ++ newLinks.map(_ -> emptyHtmlDoc).toMap
    } else {
      val generatedDocumentsAndLinks = newLinks.map{ link =>
        val nbrLinks = Random.nextInt(5) + 1
        val (htmlDoc, myLinks) = TestUtils.generateHtmlDocument(nbrLinks, 20)
        (link, htmlDoc, myLinks)
      }
      val generatedLinks = generatedDocumentsAndLinks.flatMap(_._3)
      val newLinkMapping = generatedDocumentsAndLinks.map(gen => gen._1 -> gen._2).toMap
      generateHtmlTree(generatedLinks, linkMapping ++ newLinkMapping, currentDepth + 1, maxDepth)
    }
  }

  def generateHtmlDocument(nbrLinkTags: Int, nbrOtherTags: Int): (Document, Set[String]) = {

    def generateLink(): String = {
      val depth = Random.nextInt(5) + 1
      val randomLink = (1 to depth).map(_ => Random.alphanumeric.take(Random.nextInt(10) + 1).mkString).mkString("/")
      "/" + randomLink
    }

    def generateNonLinkTag(): String = {
      val p = Random.nextInt(100)
      if (p < 25) "<a hruf=/bad/attribute></a>" // Bad attribute
      else if (p < 50) "<a href=www.somesite.com></a>" // Non local link
      else "<tag></tag>" // Not link tag
    }

    val links = (1 to nbrLinkTags).map(_ => generateLink()).toSet
    val linkTags = links.map(link => s"<a href=$link></a>")
    val otherTags = (1 to nbrOtherTags).map(_ => generateNonLinkTag())
    val htmlDoc = Jsoup.parse(Random.shuffle(linkTags ++ otherTags).mkString)
    (htmlDoc, links)
  }



}
