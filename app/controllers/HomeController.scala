package controllers


import dataStructures.morphias.MongoDatastoreFactory
import javax.inject.Inject
import models.Query
import org.mongodb.morphia.Datastore
import pipeline.CMapFinder
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport


class HomeController  @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) with I18nSupport{

  var dsOpt = Option.empty[Datastore]

  val queryForm = Form( mapping(
    "query" -> nonEmptyText
  )(Query.apply)(Query.unapply)
  )

  def index = Action { implicit request =>

    Ok(views.html.index(queryForm) )

  }

  def map = Action { implicit request =>

    val queryOpt: Option[Query] = queryForm.bindFromRequest.value

    val query: String = queryOpt match {
      case Some(query: Query) => query.query
      case _ => ""
    }

    val ds : Datastore= dsOpt match {
      case Some(dataStore) => dataStore
      case _ => val newDataStore =  MongoDatastoreFactory().createDataStore
        dsOpt = Option(newDataStore)
        newDataStore
    }

    val cMap : CMapFinder = CMapFinder(query, ds)

    val jsonStr: String = cMap.getCMapJson

    if(jsonStr != "{}"){
      Ok(views.html.map(jsonStr, queryForm))
    }else{
      if(query.isEmpty){
        Redirect("/")
      }else{
        BadRequest(s"「$query」に関するキュレーションマップはこのデータベース内にありません。")
      }
    }
  }
}

