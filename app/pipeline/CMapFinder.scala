package pipeline

import dataStructures.jsons.CurationMapJson
import dataStructures.morphias.CurationMapMorphia
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query
import play.api.libs.json.Json

case class CMapFinder(query: String, ds: Datastore) {
  val res: Query[CurationMapMorphia] = ds.createQuery(classOf[CurationMapMorphia]).field("query").equal(query)
  var cMapJsonOpt = Option.empty[CurationMapJson]

  if(res.count() != 0){
    cMapJsonOpt = Option(res.get().toJson)

  }
  def getCMapJson: String ={
    cMapJsonOpt match {
      case Some(v) => Json.toJson(v).toString()
      case _ => "{}"
    }
  }
}
