import Dependencies._

ThisBuild / scalaVersion     := "3.1.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.serviveragent"
ThisBuild / organizationName := "serviveragent"

lazy val commonSettings = Seq(
  scalafmtOnCompile := true
)

lazy val root = (project in file("."))
  .settings(
    name := "akka-sound",
    commonSettings,
    libraryDependencies += akkaActorTyped,
    libraryDependencies += scalaTest,
  )
