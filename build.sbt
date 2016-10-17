
name := "akka-db-connpool"
organization := "org.guangwenz"
version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.11"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.jolbox" % "bonecp" % "0.8.0.RELEASE"
  )
}