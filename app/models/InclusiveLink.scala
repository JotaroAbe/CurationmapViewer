package models

import java.util.UUID

case class InclusiveLink(weight: Double , destDocNum : Int){


  override def toString: String = {
    s"-> Doc$destDocNum"
  }


  def getDestDocNum : Int ={
    destDocNum
  }

/*  def getDestText : String={
    destText
  }*/

  def +(rearLink : InclusiveLink) :InclusiveLink={
    InclusiveLink((this.weight + rearLink.weight) / 2, destDocNum) //マージ戦略再考
  }
  def isLinkNone :Boolean={
    false
  }
}

class LinkNone(initDocNum : Int) extends InclusiveLink(0.0,Document.docNumNone){


  override def toString: String = {
    s"Doc${initDocNum}Frag None Link"
  }


  override def getDestDocNum: Int = Document.docNumNone

  override def isLinkNone: Boolean = true


}
object LinkNone extends InclusiveLink(0.0,  Document.docNumNone){
  def apply(initDocNum: Int): LinkNone = new LinkNone(initDocNum)
}


