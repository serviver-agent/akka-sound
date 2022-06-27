package com.serviveragent.actortest

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Amplifier:

  def apply(receiveBox: ActorRef[Signal]): Behavior[Signal] =
    amplifier(receiveBox)

  private def amplifier(
      receiveBox: ActorRef[Signal]
  ): Behavior[Signal] =
    Behaviors.receiveMessage { signal =>
      receiveBox ! Signal(signal.value * 2)
      Behaviors.same
    }
