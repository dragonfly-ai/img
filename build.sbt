ThisBuild / scalaVersion := "3.1.0"
ThisBuild / publishTo := Some( Resolver.file( "file",  new File("/var/www/maven") ) )

lazy val img = crossProject(JSPlatform, JVMPlatform).settings(
  publishTo := Some( Resolver.file( "file",  new File( "/var/www/maven" ) ) ),
  name := "img",
  version := "0.4",
  organization := "ai.dragonfly.code",
  resolvers += "dragonfly.ai" at "https://code.dragonfly.ai/",
  scalacOptions ++= Seq("-feature","-deprecation"),
  Compile / mainClass := Some("ai.dragonfly.img.TestImg"),
  libraryDependencies ++= Seq( "ai.dragonfly.code" %%% "color" % "0.3.4525" )
).jvmSettings().jsSettings()

lazy val browserDemo = project.enablePlugins(ScalaJSPlugin).dependsOn(img.projects(JSPlatform)).settings(
  name := "browserDemo",
  Compile / mainClass := Some("Demo"),
  Compile / fastOptJS / artifactPath := file("./DemoServer/public_html/js/main.js"),
  Compile / fullOptJS / artifactPath := file("./DemoServer/public_html/js/main.js"),
  scalaJSUseMainModuleInitializer := true
)

lazy val akkaHttpVersion = "10.2.7"
lazy val akkaVersion    = "2.6.17"

lazy val DemoServer = project.dependsOn(img.projects(JVMPlatform)).settings(
  name := "DemoServer",
  compile / mainClass := Some("ImageServer"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" % "akka-http_2.13" % akkaHttpVersion,
    "com.typesafe.akka" % "akka-stream_2.13" % akkaVersion // or whatever the latest version is
  )
)