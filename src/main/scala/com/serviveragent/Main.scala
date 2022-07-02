package com.serviveragent

import akka.actor.typed.ActorSystem
import com.serviveragent.schedulertest.SchedulerModule
import org.slf4j.LoggerFactory

@main def main(): Unit =
  val logger = LoggerFactory.getLogger(getClass)
  val system =
    ActorSystem(SchedulerModule.createModule(), "ActorSystem")
  logger.info("application initialized. press enter to stop")
  io.StdIn.readLine()
  system.terminate()
