ThisBuild / scalaVersion := "2.13.3"

lazy val root = project.in(file(".")).aggregate(img.js, img.jvm)

lazy val img = crossProject(JSPlatform, JVMPlatform).settings(
  publishTo := Some(Resolver.file("file",  new File("/var/www/maven"))),
  name := "img",
  version := "0.203",
  organization := "ai.dragonfly.code",
  resolvers += "dragonfly.ai" at "https://code.dragonfly.ai/",
  libraryDependencies ++= Seq( "ai.dragonfly.code" %%% "color" % "0.203" ),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  mainClass in (Compile, run) := Some("ai.dragonfly.img.TestImg")
).jvmSettings().jsSettings(
  scalaJSUseMainModuleInitializer := true
)