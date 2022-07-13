package com.serviveragent.actortest

import akka.actor.typed.{Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.NotUsed

object Module {

  def createModule(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.log.info("initializing the module...")
      val receiveBox = context.spawn(ReceiveBox(), "ReceiveBox")
      val amplifier = context.spawn(Amplifier(receiveBox), "Amplifier")

      amplifier ! Signal(0.5)

      Behaviors.receiveSignal { case (_, Terminated(_)) =>
        Behaviors.stopped
      }
    }
}
