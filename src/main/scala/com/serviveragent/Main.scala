package com.serviveragent

import akka.actor.typed.ActorSystem
import com.serviveragent.control.shutdown.GracefulShutdownDispatcher
import com.serviveragent.controller.Controller
import com.serviveragent.soundtest.AudioMain
import com.serviveragent.guitest.GUIMain

object Main extends App {

  val gracefulShutdownDispatcher = new GracefulShutdownDispatcher

  val controller = new Controller

//  val audioMain = new AudioMain(controller, gracefulShutdownDispatcher)
  val guiMain = new GUIMain(controller, gracefulShutdownDispatcher)
  val consoleMain = new ConsoleMain(System.in, controller, gracefulShutdownDispatcher)

  println("type `quit` or close window to stop all")

  gracefulShutdownDispatcher.startAll()

}
