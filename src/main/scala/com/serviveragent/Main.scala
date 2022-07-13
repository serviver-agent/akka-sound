package com.serviveragent

import akka.actor.typed.ActorSystem
import com.serviveragent.soundtest.AudioMain

object Main extends App {
  val audioMain = new AudioMain()
  new Thread(audioMain).start()
  println("press enter to stop")
  io.StdIn.readLine()
  println("stop")
  audioMain.stop()
}
