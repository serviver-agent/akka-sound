package com.serviveragent

import akka.actor.typed.ActorSystem

@main def main(): Unit =
  ActorSystem(schedulertest.SchedulerTest.createModule(), "ActorSystem")
