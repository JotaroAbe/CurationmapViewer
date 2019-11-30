package tools

import org.jsoup._
import org.jsoup.nodes.Element

case class GetterFromWeb(url : String){

  val (titleOpt: Option[String], bodyOpt: Option[Element]) = try {
    val source: nodes.Document = Jsoup.connect(url).get
    (Some(source.title()), Some(source.body))
  }
  catch{
    case e:Exception =>
      println(s"${url}の文書データを取得できませんでした。")
      (None, None)
  }
  def getBodyText : String ={
    bodyOpt match {
      case Some(value:Element) => value.text
      case _ => ""
    }
  }

  def getTitle: String={
    titleOpt match {
      case Some(value:String) => value
      case _ => ""
    }
  }


}
