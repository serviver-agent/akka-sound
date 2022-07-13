package com.serviveragent.actortest

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object ReceiveBox {

  def apply(): Behavior[Signal] = Behaviors.receive { (context, signal) =>
    context.log.info("{}", signal)
    Behaviors.same
  }
}
