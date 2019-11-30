package tools


import java.net._
import java.util
import java.util.Scanner

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URLEncoder

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable



case class BingSearcher() {

  val config: Config = ConfigFactory.load()
  private val subscriptionKey: String = config.getString("bingkey")
  val NUMBER_OF_RESULT = 50
  val URL_ONCE_NUM = 50
  val SAFE_SEARCH = "Strict"
  /*
   * If you encounter unexpected authorization errors, double-check these values
   * against the endpoint for your Bing Web search instance in your Azure
   * dashboard.
   */
  val host = "https://api.cognitive.microsoft.com"
  val path = "/bing/v7.0/search"


  var urls:List[String] = Nil

  def search(searchTerm: String): Unit ={


    if (subscriptionKey.length != 32) {
      println("Invalid Bing Search API subscription key!")
      println("Please paste yours into the source code.")
      System.exit(1)
    }

    // Call the SearchWeb method and print the response.
    try {
      System.out.print("Searching the Web for: " + searchTerm)
      var offset = 0
      while(URL_ONCE_NUM + offset <= NUMBER_OF_RESULT) {
        val result = SearchWeb(searchTerm, offset)
        urls ++= getUrlList(result.jsonResponse)
        print(".")
        offset += URL_ONCE_NUM

      }
      println()

    } catch {
      case e: Exception =>
        e.printStackTrace(System.out)
        System.exit(1)
    }

  }


  @throws[Exception]
  def  SearchWeb(searchQuery: String, offset: Int): SearchResults = synchronized{ // Construct the URL.
    wait(500)
    val url = new URL(s"$host$path?q=${URLEncoder.encode(searchQuery, "UTF-8")}&count=$URL_ONCE_NUM&safeSearch=$SAFE_SEARCH&offset=$offset")
    // Open the connection.
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey)
    // Receive the JSON response body.
    val stream = connection.getInputStream
    val response = new Scanner(stream).useDelimiter("\\A").next
    // Construct the result object.
    val results = SearchResults(new util.HashMap[String, String](), response)
    // Extract Bing-related HTTP headers.
    val headers = connection.getHeaderFields
    import scala.collection.JavaConversions._
    for (header <- headers.keySet) {
      if (header == null){
        //Do Nothing
      } else if(header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
        results.relevantHeaders.put(header, headers.get(header).get(0))
      }
    }
    stream.close()
    results
  }

  def getUrlList(jsonText: String): List[String] = {
    val ret =  mutable.MutableList.empty[String]
    val parser = new JsonParser()
    val json = parser.parse(jsonText).getAsJsonObject

    val webPages = json.getAsJsonObject("webPages")
    if(webPages != null){
      webPages.getAsJsonArray("value").forEach{
        page =>
          ret += page.asInstanceOf[JsonObject].get("url").getAsString
      }
    }
    ret.toList
  }

  def getInput : List[String] ={
    urls.distinct
  }
}


case class SearchResults(var relevantHeaders: util.HashMap[String, String], var jsonResponse: String)