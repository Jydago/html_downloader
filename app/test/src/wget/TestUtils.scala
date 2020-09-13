package wget

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.util.Random

object TestUtils {

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
