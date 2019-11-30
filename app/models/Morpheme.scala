package models

case class Morpheme(word: String, morph : String) {

  private val pos =  morph.split(",").head
  private val spos = morph match {
    case m if !m.contains(",") => "EOS"
    case _ => morph.split(",")(1)
  }

  println(s"word: $word pos: $pos spos: $spos")

  def getPartsOfSpeech:String ={
    //println(s"$word +++ ${morph.split(",").head}")
    pos
  }
  def getSubPartsOfSpeech:String ={
    //println(s"$word +++ ${morph.split(",").head}")
    spos
  }

}
