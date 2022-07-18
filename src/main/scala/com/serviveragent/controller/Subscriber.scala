package com.serviveragent.controller

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*

class Subscriber[A](init: A*)(name: String, onReceive: A => Unit) extends Thread {

  private val queue = new ConcurrentLinkedQueue[A](init.asJava)

  private var isRunning = false

  private[controller] def putAndNotify(value: A): Unit = {
    queue.add(value)
    interrupt()
  }

  private def blocking(): A = synchronized {
    Option(queue.poll()) match {
      case None    => wait(); queue.poll()
      case Some(v) => v
    }
  }

  override def run(): Unit = {
    isRunning = true

    while (isRunning) {
      try {
        onReceive(blocking())
      } catch {
        case _: InterruptedException =>
      }
    }
  }

  def shutdown(): Unit = {
    isRunning = false
    interrupt()
  }

}
