package pipeline

import dataStructures.morphias.{DocumentMorphia, Morphia2Scala}
import dev.morphia.Datastore
import dev.morphia.query.Query
import models.CurationMap
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

case class CMapFinder(query: String, alpha: Double, beta : Double, ds: Datastore, isMerge: Boolean = true, isGenSplitLink: Boolean = true) {
  val res: Query[DocumentMorphia] = ds.createQuery(classOf[DocumentMorphia]).field("query").equal(query)
  val cmapOpt: Option[CurationMap] = getCurationMap

  def isNonEmpty: Boolean ={
    res.count() != 0
  }


  private def getCurationMap: Option[CurationMap] = {
    if(res.count() != 0){

      val documentMorphias = mutable.MutableList.empty[DocumentMorphia]

      res.asList().forEach{
        docm : DocumentMorphia=>
          documentMorphias += docm
      }
      val ret = Morphia2Scala().convert(documentMorphias.toList, alpha, beta)
      ret.deleteWeakLink()
      if(isGenSplitLink){
        ret.genSplitLink()
      }
      if(isMerge){
        ret.mergeLink()
      }
      ret.calcHits()
      Some(ret)
    }else{
      None
    }
  }


  def getCMapJson: JsValue ={

    cmapOpt match {
      case Some(cmap : CurationMap) =>
        Json.toJson(cmap.toJson)
      case _ => Json.toJson("{}")
    }
  }
}
