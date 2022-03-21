ThisBuild / scalaVersion := "3.1.0"
ThisBuild / publishTo := Some( Resolver.file( "file",  new File("/var/www/maven") ) )

lazy val img = crossProject(JSPlatform, JVMPlatform).settings(
  publishTo := Some( Resolver.file( "file",  new File( "/var/www/maven" ) ) ),
  name := "img",
  version := "0.3.3.4525",
  organization := "ai.dragonfly.code",
  resolvers += "dragonfly.ai" at "https://code.dragonfly.ai/",
  scalacOptions ++= Seq("-feature","-deprecation"),
  Compile / mainClass := Some("ai.dragonfly.img.TestImg"),
  libraryDependencies ++= Seq( "ai.dragonfly.code" %%% "color" % "0.3.4525" )
).jvmSettings().jsSettings(
  scalaJSUseMainModuleInitializer := true
)
