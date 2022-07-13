import sbt._

object Dependencies {
  val AkkaActor = "2.6.19"

  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.6.1" //catsは標準ライブラリ扱い
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.10" % Test
  lazy val akkaActorTyped =
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaActor

  val logback = "ch.qos.logback" % "logback-classic" % "1.2.11"
}
