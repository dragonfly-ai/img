import sbt.Keys._

scalaVersion in ThisBuild := "2.12.0"

name in ThisBuild := "img"

organization in ThisBuild := "ai.dragonfly.code"

version in ThisBuild := "0.1"

resolvers in ThisBuild += "dragonfly.ai" at "http://code.dragonfly.ai/"

publishTo in ThisBuild := Some(Resolver.file("file",  new File( "/var/www/maven" )) )

val img = crossProject.settings(
  // shared settings
  libraryDependencies ++= Seq(
    "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.9.1",
    "com.lihaoyi" %%% "scalatags" % "0.6.2",
    "ai.dragonfly.code" %%% "color" % "0.1"
  )
).jsSettings(
  // JS-specific settings here
  jsDependencies += RuntimeDOM
).jvmSettings(
  // JVM-specific settings here
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
)

lazy val js = img.js

lazy val jvm = img.jvm
