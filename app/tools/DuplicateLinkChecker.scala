package tools

import models.{Document, Fragment, InclusiveLink}

import scala.collection.mutable

case class DuplicateLinkChecker(frag1 : Fragment, frag2 : Fragment, weight : Double) {

  var isDuplicate : Boolean = false
  private val dupDocNumSet = mutable.Set.empty[Int]

  getStrongDestLinkDocNums(frag1, weight).foreach{
    destDocNum=>
      if(getStrongDestLinkDocNums(frag2, weight).contains(destDocNum)){//重複
        dupDocNumSet += destDocNum
        isDuplicate = true
      }
  }


  private def getStrongDestLinkDocNums(frag :Fragment, weight :Double) : Set[Int]={
    val ret = mutable.Set.empty[Int]
    frag.links.foreach{
      link=>
        if(link.weight >= weight){
          ret += link.getDestDocNum
        }
    }
    ret.toSet
  }

  def getDupDocNumSet: Set[Int]={
    dupDocNumSet.toSet
  }

}
