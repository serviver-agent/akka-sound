package com.serviveragent

import akka.actor.typed.ActorSystem
import com.serviveragent.soundtest.AudioMain
import com.serviveragent.guitest.GUIMain

object Main extends App {
  val audioMain = new AudioMain()
  new Thread(audioMain).start()
  val guiMain = new GUIMain(() => println("close button pressed"))
  println("press enter to stop")
  io.StdIn.readLine()
  println("stop")
  audioMain.stop()
}
