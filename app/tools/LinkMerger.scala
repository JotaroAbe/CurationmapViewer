package tools

import models._

import scala.collection.mutable

case class LinkMerger(preFrag: Fragment, rearFrag: Fragment, currentFragList : Vector[Fragment], beta : Double) {
  val mergedLinks : mutable.MutableList[InclusiveLink] = mutable.MutableList.empty[InclusiveLink]
  val mergePreLinks : mutable.MutableList[InclusiveLink] = mutable.MutableList.empty[InclusiveLink]
  val mergeRearLinks : mutable.MutableList[InclusiveLink] = mutable.MutableList.empty[InclusiveLink]
  var mergedFrag : Fragment = FragNone
  val isMerge :Boolean = hasDuplicateLinks //条件

  if(isMerge){

    genDuplicateLinkAndFrag()

  }


  def getNewFragList: Vector[Fragment]={
    if(isMerge){
      FragListUpdater(currentFragList, preFrag, rearFrag , mergedFrag).getNewList
    }else{
      currentFragList
    }

  }

  def hasDuplicateLinks : Boolean={
    DuplicateLinkChecker(preFrag, rearFrag, beta).isDuplicate
  }

  private def genDuplicateLinkAndFrag():Unit={
    preFrag.links.foreach{
      preFragLink=>
        rearFrag.links.foreach{
          rearFragLink=>
            if(preFragLink.getDestDocNum == rearFragLink.getDestDocNum ){

              mergePreLinks += preFragLink
              mergeRearLinks += rearFragLink
            }
        }

    }
    for(i <- mergePreLinks.indices) {

      val pl = mergePreLinks.get(i) match {
        case Some(l) => l
        case _ => LinkNone
      }
      val rl = mergeRearLinks.get(i) match {
        case Some(l) => l
        case _ => LinkNone
      }

      mergedLinks += pl + rl
    }

    mergedFrag = preFrag + (rearFrag, beta)
  }

}
