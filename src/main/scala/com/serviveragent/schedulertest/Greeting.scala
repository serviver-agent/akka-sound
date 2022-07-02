package com.serviveragent.schedulertest

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Greeting:

  def apply(): Behavior[String] = Behaviors.receive { (context, message) =>
    context.log.info("{}", message)
    Behaviors.same
  }
