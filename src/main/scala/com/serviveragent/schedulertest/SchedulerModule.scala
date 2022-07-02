package com.serviveragent.schedulertest

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Scheduler, Terminated}

import java.nio.ByteBuffer
import scala.concurrent.duration.*

object SchedulerModule:

  def createModule(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val greeting = context.spawn(Greeting(), "greeting")

      val sourceDataLine = Audios.sourceDataLine

      val data: Array[Byte] = (0 until 4096).flatMap { i =>
        val sig: Float = Math.sin((i * 440f / 44100) * Math.PI * 2).toFloat
        ByteBuffer.allocate(4).putFloat(sig).array()
      }.toArray
      println(data.length)
      val greet: Runnable = () =>
        greeting ! "hoge"
        sourceDataLine.write(data, 0, data.length)

      val scheduler = context.spawn(
        SimpleScheduler(greet, (4096d / 44100).second),
        "scheduler"
      )

      Behaviors.receiveSignal { case (_, Terminated(_)) =>
        Behaviors.stopped
      }
    }
