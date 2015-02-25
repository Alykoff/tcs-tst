name := "tcs-tst"

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-remote" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-cluster" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-testkit" % "2.4-SNAPSHOT",
  "org.fusesource" % "sigar" % "1.6.4",
  "io.kamon" % "sigar-loader" % "1.6.5-rev001",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.ning" % "async-http-client" % "1.9.10",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

//retrieveManaged := true