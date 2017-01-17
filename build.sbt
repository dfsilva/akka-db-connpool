
name := "akka-db-connpool"
organization := "org.guangwenz"
version := "1.1.0"

scalaVersion := "2.12.1"

val akkaVersion = "2.4.16"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.jolbox" % "bonecp" % "0.8.0.RELEASE",

    "org.scalactic" %% "scalactic" % "3.0.0",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "org.apache.derby" % "derby" % "10.12.1.1" % "test"
  )
}