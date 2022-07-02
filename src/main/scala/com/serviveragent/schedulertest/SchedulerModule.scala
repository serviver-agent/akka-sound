package com.serviveragent.schedulertest

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Scheduler, Terminated}

import scala.concurrent.duration.*

object SchedulerModule:

  def createModule(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val greeting = context.spawn(Greeting(), "greeting")
      val greet: Runnable = () => greeting ! "hoge"

      val scheduler = context.spawn(
        SimpleScheduler(greet, 1.second),
        "scheduler"
      )

      Behaviors.receiveSignal { case (_, Terminated(_)) =>
        Behaviors.stopped
      }
    }
