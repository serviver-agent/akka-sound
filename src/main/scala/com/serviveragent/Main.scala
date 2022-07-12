package com.serviveragent

import akka.actor.typed.ActorSystem
import com.serviveragent.soundtest.AudioMain

@main def main(): Unit =
  val audioMain = new AudioMain()
  new Thread(audioMain).run()
  io.StdIn.readLine()
  println("stop")
  audioMain.stop()
