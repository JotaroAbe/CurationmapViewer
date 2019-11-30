package tools

import java.net.URL

import com.google.api.client.http.HttpRequest
import com.google.api.services.customsearch.model.{Result, Search}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.customsearch.Customsearch


case class GoogleSearcher(query : String) {
  val config: Config = ConfigFactory.load()
  val HTTP_REQUEST_TIMEOUT = 1000
  val URL_ONCE_NUM = 10
  val GET_URL_NUM = 100

  val customsearch = new Customsearch(new NetHttpTransport, new JacksonFactory, new HttpRequestInitializer() {
    def initialize(httpRequest: HttpRequest): Unit = {
      try { // set connect and read timeouts
        httpRequest.setConnectTimeout(HTTP_REQUEST_TIMEOUT)
        httpRequest.setReadTimeout(HTTP_REQUEST_TIMEOUT)
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
      }
    }
  })

  var startIndex = 1
  val urls: mutable.MutableList[String] = mutable.MutableList.empty[String]
  while(startIndex < GET_URL_NUM) {

    val list: Customsearch#Cse#List = customsearch.cse.list(query)
    list.setKey(config.getString("csekey"))
    list.setCx(config.getString("csecx"))
    list.setNum(URL_ONCE_NUM.toLong)
    list.setStart(startIndex.toLong)
    val results: Search = list.execute
    val resultList: java.util.List[Result] = results.getItems

    resultList.forEach {
      result =>
        if(result.getFormattedUrl.startsWith("http")){
          urls += result.getFormattedUrl
        }else{
          urls += s"http://${result.getFormattedUrl}"
        }
    }

    startIndex += startIndex + URL_ONCE_NUM
  }

  def getInput : List[String] ={
    urls.toList
  }

}
