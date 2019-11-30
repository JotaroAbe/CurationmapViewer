package tools

import models.{CurationMap, Document, Fragment, Morpheme}

import scala.collection.mutable

import us.feliscat.text.analyzer.mor.mecab.IpadicMecab



import us.feliscat.text.StringOption

case class DataInputer(){

  val docList: mutable.MutableList[Document] = mutable.MutableList.empty[Document]

  def inputWebData(query : String, sourceList : List[String]): CurationMap ={
   println("Web文書読み込み開始")
    var i :Int = 0
    sourceList.foreach {
      source =>
        if(source.nonEmpty) {
          i += 1
          println(s"$i / ${sourceList.length}")
          println(s"source URL:$source")
          val fragList = mutable.MutableList.empty[Fragment]
          val webData = GetterFromWeb(source)
          val docStr: String = webData.getBodyText
          val docTitle: String = webData.getTitle

          if(docStr.nonEmpty) {
            val queue: mutable.MutableList[Morpheme] = mutable.MutableList.empty[Morpheme]
            println("形態素解析中...")
            docStr.split("。").foreach {
              str =>
                if (str.nonEmpty) {
                  IpadicMecab.analyze(StringOption(str + "。")).foreach {
                    mor =>
                      //println(mor)
                      val m = Morpheme(mor.split("\t").head, mor.split("\t").last)
                      if (m.morph != "EOS") {
                        queue += m
                        if (queue.last.getSubPartsOfSpeech == "句点") {
                          fragList += Fragment(queue.toVector)
                          //println(doc.fragList.last.getText())
                          queue.clear()
                        }
                      }
                  }
                }
            }
            val doc: Document = Document(source, docTitle, fragList.toVector, sourceList.indexOf(source))
            doc.setDocNumToFrag()

            docList += doc
          }
        }
    }
    CurationMap(query,docList.toVector, CurationMap.DEFAULT_ALPHA, CurationMap.DEFAULT_BETA )
  }



}
