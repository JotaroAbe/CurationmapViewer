name := "CurationmapGenerator"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.1"
libraryDependencies += guice
libraryDependencies += "us.feliscat" % "feliscatuszerolibraries_2.12" % "0.0.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0"
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.1"
libraryDependencies ++= Seq(
  jdbc,
  "dev.morphia.morphia" % "core" % "1.5.8",
  "org.mongodb" % "mongo-java-driver" % "3.12.0",
"com.google.apis" % "google-api-services-customsearch" % "v1-rev73-1.25.0"
)
import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport := Seq.empty
