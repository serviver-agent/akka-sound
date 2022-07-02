package com.serviveragent.schedulertest

import akka.NotUsed
import akka.actor.typed.{Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

object SchedulerTest:

  def createModule(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val greeting = context.spawn(Greeting(), "greeting")
      greeting ! "hello"

      Behaviors.receiveSignal { case (_, Terminated(_)) =>
        Behaviors.stopped
      }
    }

object Greeting:

  def apply(): Behavior[String] = Behaviors.receiveMessage { message =>
    println(message)
    Behaviors.same
  }
