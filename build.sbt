ThisBuild / scalaVersion := "2.13.3"

lazy val root = project.in(file(".")).aggregate(img.js, img.jvm).settings(
  publishTo := Some( Resolver.file("file",  new File( "/var/www/maven" ) ) )
)

lazy val img = crossProject(JSPlatform, JVMPlatform).settings(
  publishTo := Some(Resolver.file("file",  new File("/var/www/maven"))),
  name := "img",
  version := "0.2",
  organization := "ai.dragonfly.code",
  resolvers += "dragonfly.ai" at "https://code.dragonfly.ai:4343/",
  libraryDependencies ++= Seq( "ai.dragonfly.code" %%% "color" % "0.2" ),
  scalacOptions ++= Seq("-feature"),
  mainClass in (Compile, run) := Some("ai.dragonfly.img.TestImg")
).jvmSettings().jsSettings(
  scalaJSUseMainModuleInitializer := true
)