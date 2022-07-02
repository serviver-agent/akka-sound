package com.serviveragent.schedulertest

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}

object SimpleScheduler:

  def apply(run: Runnable, delay: FiniteDuration): Behavior[Unit] =
    Behaviors.setup { context =>
      val scheduler = context.system.scheduler
      implicit val ec: ExecutionContext = context.executionContext
      scheduler.scheduleWithFixedDelay(Duration.Zero, delay)(run)
      Behaviors.empty
    }
