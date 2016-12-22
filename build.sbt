scalaVersion in ThisBuild := "2.12.0"

name in ThisBuild := "img"

organization in ThisBuild := "ai.dragonfly.code"

version in ThisBuild := "0.1"

publishTo in ThisBuild := Some(Resolver.file("file",  new File( "/var/www/maven" )) )

val img = crossProject.settings(
  // shared settings
).jsSettings(
  // JS-specific settings here
).jvmSettings(
  // JVM-specific settings here
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
)

lazy val js = img.js

lazy val jvm = img.jvm
