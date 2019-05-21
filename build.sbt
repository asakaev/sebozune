import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name := "sebozune"
inThisBuild(
  Seq(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.8",
    organization := "io.github.asakaev",
    scalacOptions ++= List(
      "-deprecation",
      "-Xlint",
//      "-Xfatal-warnings",
      "-language:higherKinds",
      "-Ypartial-unification"
    )
  )
)

lazy val root =
  project
    .in(file("."))
    .aggregate(backendJVM, sharedJVM, frontendJS, sharedJS)
    .disablePlugins(RevolverPlugin)

lazy val backend =
  crossProject(JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(name := "sebozune-backend")
    .settings(
      libraryDependencies ++= Dependencies.backendDeps
    )
    .dependsOn(shared % "test->test;compile->compile")

lazy val frontend =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .settings(name := "sebozune-frontend")
    .settings(scalaJSUseMainModuleInitializer := true)
    .settings(
      libraryDependencies ++= Dependencies.frontendDeps.value
    )
    .dependsOn(shared % "test->test;compile->compile")
    .disablePlugins(RevolverPlugin)

lazy val shared =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .settings(
      libraryDependencies ++= Dependencies.crossDeps.value,
      libraryDependencies ++= Dependencies.crossTestDeps.value
    )
    .disablePlugins(RevolverPlugin)

lazy val backendJVM = backend.jvm
lazy val sharedJVM  = shared.jvm
lazy val frontendJS = frontend.js
lazy val sharedJS   = shared.js
