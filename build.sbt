name := """moonads"""

version := "1.0"

scalaVersion := "2.12.1"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

libraryDependencies += "org.typelevel" %% "cats" % "0.8.1"

//scalaOrganization in ThisBuild := "org.typelevel"
// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

