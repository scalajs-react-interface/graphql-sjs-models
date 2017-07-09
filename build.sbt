resolvers += Resolver.sonatypeRepo("releases")
enablePlugins(ScalaJSPlugin)
name := "graphql-sjs-models"
scalaVersion := "2.12.2"
scalaJSUseMainModuleInitializer := true
scalaJSModuleKind := ModuleKind.CommonJSModule

val LIB_MAIN = new File("./lib/main.js")

artifactPath in Compile in fullOptJS := LIB_MAIN

libraryDependencies += "io.scalajs" %%% "nodejs" % "0.4.0-pre5"