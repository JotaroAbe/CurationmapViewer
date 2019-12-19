package controllers

import java.util.UUID

import dataStructures.morphias.MongoDatastoreFactory
import javax.inject.Inject
import models._
import org.mongodb.morphia.Datastore
import pipeline.CMapFinder
import play.api.mvc.{AbstractController, ControllerComponents, WrappedRequest}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import tools.UniqueId


class HomeController  @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) with I18nSupport{

  var dsOpt = Option.empty[Datastore]

  var cmapOpt = Option.empty[CurationMap]

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
    val isMerge: Boolean = request.getQueryString("merge") match {
      case Some(ism: String) =>
        if(ism == "true"){
          true
        }else{
          false
        }
      case _ => false
    }
    val isGenSplitLink: Boolean = request.getQueryString("gensplitlink") match {
      case Some(isg: String) =>
        if(isg == "true"){
          true
        }else{
          false
        }
      case _ => false
    }

    if(!query.isEmpty && alpha >= 0.0 && alpha <= 1.0 && beta >= 0.0 && beta <= 1.0){

      val ds : Datastore= dsOpt match {
        case Some(dataStore) => dataStore
        case _ => val newDataStore =  MongoDatastoreFactory().createDataStore
          dsOpt = Option(newDataStore)
          newDataStore
      }

      val cMap : CMapFinder = CMapFinder(query, alpha, beta, ds, isMerge, isGenSplitLink)

      cmapOpt = cMap.cmapOpt

      Ok(cMap.getCMapJson)
    }else{
      BadRequest("Header Error")
    }

  }

  def getDestFrag =Action { implicit request =>

    val fragUuid: String = request.getQueryString("frag") match {
      case Some(uuid : String) => uuid
      case _ => ""
    }

    val destUuid: String= request.getQueryString("destdoc") match {
      case Some(uuid : String) => uuid
      case _ => ""
    }

    cmapOpt match {
      case Some(c : CurationMap) =>

        var initFrag: Fragment = FragNone
        var destDoc: Document = DocumentNone

        c.documents.foreach{
          doc =>

            if(doc.id.toString == destUuid){
              destDoc = doc
            }


            doc.fragList.foreach{
              frag =>
                if(frag.id.toString == fragUuid){
                  initFrag = frag
                }
            }
        }

        if(initFrag != FragNone && destDoc != DocumentNone){
          var changeText : String = ""
          initFrag.links.foreach{
            link =>
              if(destDoc.docNum == link.destDocNum){

                //var changeUuid: UUID = null

                var maxInclusive : Double = 0.0
                destDoc.fragList.foreach{
                  destFrag =>
                    val thisInclusive = initFrag.calcInclusive(destFrag)
                    if(thisInclusive > maxInclusive//&& thisInclusive > CurationMap.ALPHA テキスト断片にしなきゃ爆発するのでとりあえず
                    //&& initFrag.getText.length < destFrag.getText.length
                    ){
                      maxInclusive = thisInclusive
                      changeText = destFrag.getText
                      //changeUuid = destFrag.uuid
                    }
                }
                if(!changeText.isEmpty) {
                  //link.destText = changeText
                  //link.destUuid = changeUuid
                }
                //println(s"Doc${doc.docNum} -> $changeText")
              }
          }
          Ok(changeText)
        }else{
          val u = UniqueId.getInstance().createId()

          println(u)

          val f = Fragment(Vector.empty[Morpheme], u)

          println(f.id)


          BadRequest(initFrag.getClass.getCanonicalName+ " " + destDoc.getClass.getCanonicalName)
        }




      case _ => BadRequest("Internal Curationmap is not found")
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


