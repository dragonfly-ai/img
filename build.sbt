import sbtcrossproject.CrossPlugin.autoImport.crossProject

val sharedSettings = Seq(
  version in ThisBuild := "0.2",
  scalaVersion := "2.12.6",
  organization in ThisBuild := "ai.dragonfly.code",
  scalacOptions in ThisBuild ++= Seq("-feature"),
  resolvers in ThisBuild += "dragonfly.ai" at "https://code.dragonfly.ai/",
  libraryDependencies ++= Seq( "ai.dragonfly.code" %%% "color" % "0.2" ),
  mainClass := Some("ai.dragonfly.img.TestImg"),
  publishTo in ThisBuild := Some( Resolver.file ( "file",  new File( "/var/www/maven" ) ) )
)

val img = crossProject(JSPlatform, JVMPlatform)
  .settings(sharedSettings)
  .jsSettings(scalaJSUseMainModuleInitializer := true)
