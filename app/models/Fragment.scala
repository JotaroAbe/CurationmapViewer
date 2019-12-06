package models

import java.util.UUID

import org.apache.lucene.search.Weight

import scala.collection.immutable.List
import scala.collection.mutable

case class Fragment (morphList: Vector[Morpheme], uuid :UUID = UUID.randomUUID){

  var docNum : Int = Document.docNumNone

  var links: mutable.MutableList[InclusiveLink] = mutable.MutableList.empty[InclusiveLink]

  def getText: String ={
    var ret :String = ""
    morphList.foreach{
      morph =>
        ret += morph.word
    }
    ret
  }

  def getNounList: List[String]={
    val nounList = mutable.MutableList.empty[String]

    morphList.foreach{
      morphme=>
        if(morphme.getPartsOfSpeech == "名詞"){
          nounList += morphme.word
        }
    }

    nounList.toList
  }


  def hasLink(docNum : Int, alpha : Double): Boolean ={
    var ret : Boolean = false
    links.foreach{
      link =>
        if(link.getDestDocNum == docNum && link.weight >= alpha){
          ret = true
        }
    }
    ret
  }

  def genLink(destDoc : Document): Unit ={
    var link : InclusiveLink = LinkNone.apply(Document.docNumNone)
    if (docNum != destDoc.docNum) {
      val inclusiveScore: Double = calcInclusive(destDoc)
      //println(s"$inclusiveScore")
      //if (inclusiveScore >= CurationMap.ALPHA) {
        println(s"Doc${this.docNum} -> Doc${destDoc.docNum} Weight: $inclusiveScore")
        link = InclusiveLink(inclusiveScore, destDoc.docNum)
      //} else {
        //frag.links += NoneLink(doc.docNum)
      //}
    }else{
      //frag.links += NoneLink(doc.docNum)
    }
    if(!link.isLinkNone){
      links += link
    }

  }

  def genLink(destDoc : Document, weight : Double): Unit ={
    var link : InclusiveLink = LinkNone.apply(Document.docNumNone)
    if (docNum != destDoc.docNum) {
      //val inclusiveScore: Double = calcInclusive(destDoc)
      //println(s"$inclusiveScore")
      //if (inclusiveScore >= CurationMap.ALPHA) {
      println(s"Doc${this.docNum} -> Doc${destDoc.docNum} Weight: $weight")
      link = InclusiveLink(weight, destDoc.docNum)
      //} else {
      //frag.links += NoneLink(doc.docNum)
      //}
    }else{
      //frag.links += NoneLink(doc.docNum)
    }
    if(!link.isLinkNone){
      links += link
    }

  }


  def +(rearFrag : Fragment) :Fragment ={
    val mergedFrag = Fragment(Vector.concat(morphList, rearFrag.morphList), this.uuid)

    mergedFrag.docNum = docNum
    this.links.foreach{
      preLink =>
        rearFrag.links.foreach{
          rearLink=>
          if(preLink.destDocNum == rearLink.destDocNum){
            mergedFrag.links += preLink + rearLink
          }
        }
    }
    mergedFrag
  }

  def calcInclusive(destDoc :Document) : Double={
    val nounNum :Int= getNounList.length
    var inclusiveNum : Int= 0

    getNounList.foreach{
      initNoun=>
        if(destDoc.getNounList.contains(initNoun)){
          inclusiveNum += 1
        }
    }

    if(nounNum != 0 && inclusiveNum != 0){
      inclusiveNum.toFloat / nounNum
    }else{
      0.0
    }


  }
  def calcInclusive(destFrag :Fragment) : Double={
    val nounNum :Int= getNounList.length
    var inclusiveNum : Int= 0

    getNounList.foreach{
      initNoun=>
        if(destFrag.getNounList.contains(initNoun)){
          inclusiveNum += 1
        }
    }

    if(nounNum != 0 && inclusiveNum != 0){
      inclusiveNum.toFloat / nounNum
    }else{
      0.0
    }


  }

  def hasStrongLink(alpha: Double) : Boolean={
    var ret = false

    links.foreach{
      l =>
        if(l.weight >= alpha ){
          ret = true
        }
    }

    ret
  }

  def isFragNone :Boolean={
    false
  }

}
object FragNone extends Fragment(Vector.empty) {
  override def isFragNone :Boolean={
    true
  }
}
