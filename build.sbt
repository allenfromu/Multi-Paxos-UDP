name := "Multi-Paxos"

version := "0.1"

mainClass := Some("Impl.Main")

scalaVersion := "2.11.5"

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.3.12",
                 "com.typesafe.akka" %% "akka-remote" % "2.3.12")
                            


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
