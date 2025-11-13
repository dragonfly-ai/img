
val appVersion:String = "0.1"
val globalScalaVersion = "3.3.7"

ThisBuild / organization := "ai.dragonfly"
ThisBuild / organizationName := "dragonfly.ai"
ThisBuild / startYear := Some(2020)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List( tlGitHubDev("dragonfly-ai", "dragonfly.ai") )
ThisBuild / scalaVersion := globalScalaVersion

ThisBuild / tlSitePublishBranch := Some("main")

ThisBuild / tlBaseVersion := appVersion
ThisBuild / tlCiReleaseBranches := Seq()

ThisBuild / nativeConfig ~= {
  _.withLTO(scala.scalanative.build.LTO.thin)
    .withMode(scala.scalanative.build.Mode.releaseFast)
    .withGC(scala.scalanative.build.GC.commix)
}

lazy val img = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "img",
    libraryDependencies ++= Seq( "ai.dragonfly" %%% "uriel" % "0.13")
  ).jsSettings(
    libraryDependencies ++= Seq( "org.scala-js" %%% "scalajs-dom" % "2.8.1"),
    scalaJSUseMainModuleInitializer := true
  ).jvmSettings(
    libraryDependencies ++= Seq( "org.scala-js" %% "scalajs-stubs" % "1.1.0")
  )

lazy val browserDemo = project.enablePlugins(ScalaJSPlugin).dependsOn(img.projects(JSPlatform)).settings(
  name := "browserDemo",
  Compile / mainClass := Some("Demo"),
  Compile / fastOptJS / artifactPath := file("./docs/js/main.js"),
  Compile / fullOptJS / artifactPath := file("./docs/js/main.js"),
  scalaJSUseMainModuleInitializer := true
)

lazy val root = tlCrossRootProject.aggregate(img, tests).settings(name := "img")

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin).settings(
  mdocVariables := Map(
    "VERSION" -> appVersion,
    "SCALA_VERSION" -> globalScalaVersion
  ),
  laikaConfig ~= { _.withRawContent }
)

lazy val unidocs = project
  .in(file("unidocs"))
  .enablePlugins(TypelevelUnidocPlugin) // also enables the ScalaUnidocPlugin
  .settings(
    name := "img-docs",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(img.jvm, img.js, img.native)
  )

lazy val tests = crossProject(
  JVMPlatform,
  JSPlatform,
  NativePlatform
)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(img)
  .settings(
    name := "img-tests",
    libraryDependencies += "org.scalameta" %%% "munit" % "1.2.1" % Test
  )