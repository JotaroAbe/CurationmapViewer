package models

import java.util.UUID

import dataStructures.jsons
import dataStructures.jsons.{CurationMapJson, DocumentJson, FragmentJson, LinkJson}
import dataStructures.morphias.{CurationMapMorphia, DocumentMorphia, FragmentMorphia, LinkMorphia}
import play.api.libs.json._
import tools.{DuplicateLinkChecker, LinkMerger}

import scala.collection.mutable
import scala.collection.JavaConverters._

object CurationMap{
  final val DEFAULT_ALPHA : Double = 0.6
  final val DEFAULT_BETA : Double = 0.6
  final val EPSILON : Double = 0.0001
}

case class CurationMap(query : String, documents : Vector[Document], alpha : Double, beta : Double) {

  documents.foreach{
    doc =>
      doc.setDocNumToFrag()
  }

  def genLink(): Unit ={

    var i : Int = 0
    println("リンク生成中...")
    documents.foreach {
      doc =>
        i += 1
        println(s"$i / ${documents.size}")
        doc.fragList.foreach {
          frag =>
            documents.foreach {
              destDoc =>
                if(frag.docNum != destDoc.docNum) {
                  frag.genLink(destDoc)
                }
            }
        }
    }
  }

  def deleteWeakLink(): Unit ={

    val weakWeight: Double =
      if (alpha <= beta){
        alpha
      }else{
        beta
      }

    documents.foreach{
      doc =>
        doc.fragList.foreach{
          frag =>
            val newLinks = mutable.MutableList.empty[InclusiveLink]
            frag.links.foreach{
              link =>
                if(link.weight >= weakWeight){
                  newLinks += link
                }
            }
            frag.links = newLinks
        }
    }
  }

  def genSplitLink(): Unit={

    println("リンク補間中...")
    documents.foreach {
      doc =>
        if(doc.fragList.size >= 3){
          val newFragList = mutable.MutableList.empty[Fragment]
          var preFrag: Fragment = FragNone
          var centerFrag: Fragment = FragNone
          doc.fragList.foreach {
            rearFrag =>
              if (!preFrag.isFragNone && !centerFrag.isFragNone) {
                if(!centerFrag.hasStrongLink(alpha)) {
                  val d = DuplicateLinkChecker(preFrag, rearFrag, alpha)
                  d.getDupDocNumSet.foreach {
                    docNum =>
                      centerFrag.genLink(getDocument(docNum), alpha)
                  }
                }
                newFragList += preFrag
              }
              preFrag = centerFrag
              centerFrag = rearFrag
          }
          newFragList += preFrag
          newFragList += centerFrag
          doc.fragList = newFragList.toVector
        }

    }
  }

  def mergeLink(): Unit ={
    println("リンク併合中...")
    var loop : Boolean = false

    do {
      loop =false
      documents.foreach {
        doc =>
          var preFrag: Fragment = FragNone
          var currentFragList = doc.fragList
          doc.fragList.foreach {
            frag =>
              if(!loop){

                val lm = LinkMerger(preFrag, frag, currentFragList, beta)
                preFrag = frag

                if (lm.isMerge) {
                  currentFragList = lm.getNewFragList
                  loop = true
                }
              }
          }

          doc.fragList = currentFragList
      }
    }while(loop)


  }

  def calcHits(): Unit= {
    println("HITS計算中...")
    documents.foreach{
      doc=>
        doc.initHitsCalc(alpha)
    }
    do {
      documents.foreach {
        doc =>
          doc.updatePreValue()
      }
      documents.foreach {
        doc =>
          doc.calcHitsOnce(documents, alpha)
      }
      documents.foreach {
        doc =>
          doc.hitsNormalize(getHubSum, getAuthSum)
      }
    }while(!isEndCalc)
  }

  private def getHubSum : Double={
    var ret :Double = 0.0
    documents.foreach{
      doc =>
        ret += doc.currentHub
    }
    ret
  }

  private def getAuthSum : Double={
    var ret :Double = 0.0
    documents.foreach{
      doc =>
        ret += doc.currentAuth
    }
    ret
  }

  private def isEndCalc :Boolean={
    var sum :Double = 0.0

    documents.foreach{
      doc =>
        sum += Math.sqrt((doc.currentHub - doc.preHub) * (doc.currentHub - doc.preHub)
          + (doc.currentAuth - doc.preAuth) *  (doc.currentAuth - doc.preAuth))
    }
    sum < CurationMap.EPSILON
  }

  def changeLinkDest() : Unit={
    println("リンク先文章選択中...")
    documents.foreach{
      doc =>
        doc.fragList.foreach{
          initfrag =>
            initfrag.links.foreach{
              link =>
                documents.foreach{
                  destDoc =>
                    if(destDoc.docNum == link.destDocNum){
                      var changeText : String = ""
                      var changeUuid: UUID = null

                      var maxInclusive : Double = 0.0
                      destDoc.fragList.foreach{
                        destFrag =>
                          val thisInclusive = initfrag.calcInclusive(destFrag)
                          if(thisInclusive > maxInclusive//&& thisInclusive > CurationMap.ALPHA テキスト断片にしなきゃ爆発するのでとりあえず
                            && initfrag.getText.length < destFrag.getText.length){
                            maxInclusive = thisInclusive
                            changeText = destFrag.getText
                            //changeUuid = destFrag.uuid
                          }
                      }
                      if(!changeText.isEmpty) {
                        //link.destText = changeText
                        //link.destUuid = changeUuid
                      }
                      println(s"Doc${doc.docNum} -> $changeText")
                    }
                }

            }
        }
    }
  }

  def toJson : CurationMapJson={
    val documentJsons = mutable.MutableList.empty[DocumentJson]
    val fragmentJsons = mutable.MutableList.empty[FragmentJson]
    val linkJsons = mutable.MutableList.empty[LinkJson]

    documents.foreach{
      doc =>
        fragmentJsons.clear
        doc.fragList.foreach{
          frag =>
            linkJsons.clear
            frag.links.foreach{
              link =>
                linkJsons += LinkJson(link.getDestDocNum, link.weight)
            }
            fragmentJsons += jsons.FragmentJson(frag.getText, linkJsons.toList, frag.id.toString)
        }
        documentJsons += jsons.DocumentJson(doc.url, doc.title, doc.docNum, doc.currentHub, doc.currentAuth, fragmentJsons.toList, doc.id.toString)
    }
    jsons.CurationMapJson(query, alpha, beta,documentJsons.toList)
  }

  def getMorphia : CurationMapMorphia={
    val documentMorphia = mutable.MutableList.empty[DocumentMorphia]
    val fragmentMorphia = mutable.MutableList.empty[FragmentMorphia]
    val linkMorphia = mutable.MutableList.empty[LinkMorphia]

    documents.foreach{
      doc =>
        fragmentMorphia.clear
        doc.fragList.foreach{
          frag =>
            linkMorphia.clear
            frag.links.foreach{
              link =>
                linkMorphia += new LinkMorphia(link.getDestDocNum, link.weight)
            }
            fragmentMorphia += new FragmentMorphia(frag.morphList.toList.asJava,linkMorphia.toList.asJava, frag.id.toString)
        }
        documentMorphia += new DocumentMorphia(doc.url, doc.title, doc.docNum, fragmentMorphia.toList.asJava, doc.id.toString)
    }
    new CurationMapMorphia(query, documentMorphia.toList.asJava)
  }

  def getText : String={
    var ret = ""
    documents.foreach{
      doc=>
        ret += s"${doc.docNum}:\n"
        doc.fragList.foreach{
          frag=>
            ret += s"${frag.getText}\n"
        }
    }
    ret
  }

  def getDocument(docNum :Int): Document ={
    var ret : Document= DocumentNone
    documents.foreach{
      doc=>
        if(doc.docNum == docNum){
          ret = doc
        }
    }
    ret
  }
}