package controllers

import dataStructures.morphias.MongoDatastoreFactory
import javax.inject.Inject
import org.mongodb.morphia._
import pipeline.CMapFinder
import play.api.mvc.{AbstractController, ControllerComponents}


class HomeController  @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  var dsOpt = Option.empty[Datastore]

  def index = Action {

    val ds : Datastore= dsOpt match {
      case Some(dataStore) => dataStore
      case _ => val newDataStore =  MongoDatastoreFactory().createDataStore
        dsOpt = Option(newDataStore)
        newDataStore
    }

    val cMap : CMapFinder = CMapFinder("桜木町", ds)

    Ok(views.html.index(cMap.getCMapJson))

  }
}
