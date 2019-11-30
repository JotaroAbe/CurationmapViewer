package pipeline

import dataStructures.jsons.CurationMapJson
import dataStructures.morphias.{CurationMapMorphia, Morphia2Scala}
import models.CurationMap
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query
import play.api.libs.json.Json

case class CMapFinder(query: String, alpha: Double, beta : Double, ds: Datastore) {
  val res: Query[CurationMapMorphia] = ds.createQuery(classOf[CurationMapMorphia]).field("query").equal(query)


 def getCurationMap: Option[CurationMap] = {
   if(res.count() != 0){
     Some(Option(res.get()) match {
       case Some(cmap : CurationMapMorphia) =>
         val ret = Morphia2Scala().convert(cmap, alpha, beta)
         ret.deleteWeakLink()
         ret.genSplitLink()
         ret.mergeLink()
         ret.calcHits()
         ret
       case _ => null
     })
   }else{
     None
   }
 }


  def getCMapJson: String ={

    getCurationMap match {
      case Some(cmap : CurationMap) =>
        Json.toJson(cmap.toJson).toString()
      case _ => "{}"
    }
  }
}
