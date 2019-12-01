package controllers

import dataStructures.morphias.MongoDatastoreFactory
import javax.inject.Inject
import models.{CurationMap, Query}
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

    val cMapF : CMapFinder = CMapFinder(query, CurationMap.DEFAULT_ALPHA, CurationMap.DEFAULT_BETA, ds)

    if(cMapF.isNonEmpty){
      Ok(views.html.map(query , queryForm))
    }else{
      if(query.isEmpty){
        Redirect("/")
      }else{
        BadRequest(s"「$query」に関するキュレーションマップはこのデータベース内にありません。")
      }
    }
  }

  def getMap= Action { implicit request =>

    val query: String = request.getQueryString("query") match {
      case Some(q: String) => q
      case _ => ""
    }
    val alpha: Double = request.getQueryString("alpha") match {
      case Some(a: String) =>
        a.toDoubleOrElse()
      case _ => -1D
    }

    val beta: Double = request.getQueryString("beta") match {
      case Some(b: String) =>
        b.toDoubleOrElse()
      case _ => -1D
    }

    if(!query.isEmpty && alpha >= 0.0 && alpha <= 1.0 && beta >= 0.0 && beta <= 1.0){

      val ds : Datastore= dsOpt match {
        case Some(dataStore) => dataStore
        case _ => val newDataStore =  MongoDatastoreFactory().createDataStore
          dsOpt = Option(newDataStore)
          newDataStore
      }

      val cMap : CMapFinder = CMapFinder(query, alpha, beta, ds)

      Ok(cMap.getCMapJson)
    }else{
      BadRequest("Header Error")
    }

  }

  implicit class StringConversion(val s: String) {

    private def toTypeOrElse[T](convert: String=>T, defaultVal: T) = try {
      convert(s)
    } catch {
      case _: NumberFormatException => defaultVal
    }

    def toShortOrElse(defaultVal: Short = 0): Short = toTypeOrElse[Short](_.toShort, defaultVal)
    def toByteOrElse(defaultVal: Byte = 0): Byte = toTypeOrElse[Byte](_.toByte, defaultVal)
    def toIntOrElse(defaultVal: Int = 0): Int = toTypeOrElse[Int](_.toInt, defaultVal)
    def toDoubleOrElse(defaultVal: Double = -1D): Double = toTypeOrElse[Double](_.toDouble, defaultVal)
    def toLongOrElse(defaultVal: Long = 0L): Long = toTypeOrElse[Long](_.toLong, defaultVal)
    def toFloatOrElse(defaultVal: Float = 0F): Float = toTypeOrElse[Float](_.toFloat, defaultVal)
  }

}


