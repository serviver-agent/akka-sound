package com.serviveragent

import akka.actor.typed.ActorSystem
import com.serviveragent.control.shutdown.GracefulShutdownDispatcher
import com.serviveragent.soundtest.AudioMain
import com.serviveragent.guitest.GUIMain

object Main extends App {

  val gracefulShutdownDispatcher = new GracefulShutdownDispatcher

  val audioMain = new AudioMain(gracefulShutdownDispatcher)
  val guiMain = new GUIMain(gracefulShutdownDispatcher)
  val consoleMain = new ConsoleMain(System.in, audioMain, gracefulShutdownDispatcher)

  println("type `quit` or close window to stop all")

  gracefulShutdownDispatcher.startAll()

}
