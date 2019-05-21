import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._

object Dependencies {
  lazy val catsVersion  = "1.6.0"
  lazy val fs2Version   = "1.0.4"
  lazy val circeVersion = "0.10.0"

  lazy val scalaJsDomVersion = "0.9.7"
  lazy val scalaTagsVersion  = "0.6.7"

  lazy val scalaTestVersion  = "3.0.5"
  lazy val scalaCheckVersion = "1.14.0"

  lazy val http4sVersion  = "0.20.1"
  lazy val logbackVersion = "1.2.3"

  val frontendDeps = Def.setting(
    List(
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
      "com.lihaoyi"  %%% "scalatags"   % scalaTagsVersion
    )
  )

  val backendDeps = List(
    "org.http4s"     %% "http4s-blaze-server" % http4sVersion,
    "org.http4s"     %% "http4s-dsl"          % http4sVersion,
    "ch.qos.logback" % "logback-classic"      % logbackVersion
  )

  val crossDeps = Def.setting(
    List(
      "org.typelevel" %%% "cats-core" % catsVersion,
      "co.fs2"        %%% "fs2-core"  % fs2Version
    ) ++ List(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion)
  )

  val crossTestDeps = Def.setting(
    List(
      "org.scalatest"  %%% "scalatest"  % scalaTestVersion,
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion
    ).map(_ % Test)
  )

}
