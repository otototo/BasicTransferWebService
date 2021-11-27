name := "BasicTransferWebService"

version := "0.1"

scalaVersion := "2.13.7"
val AkkaHttpVersion = "10.2.7"
val AkkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.7",

  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalactic" %% "scalactic" % "3.2.9" % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)