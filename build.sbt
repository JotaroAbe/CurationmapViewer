name := "CurationmapGenerator"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.1"
libraryDependencies += guice
libraryDependencies ++= Seq(
  jdbc,
  "org.mongodb.morphia" % "morphia" % "1.3.2",
  "org.mongodb" % "mongo-java-driver" % "3.7.1"

)
import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport := Seq.empty
