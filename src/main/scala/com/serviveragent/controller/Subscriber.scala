package com.serviveragent.controller

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*

class Subscriber[A](init: A*)(name: String, onReceive: A => Unit) extends Thread {

  private val queue = new ConcurrentLinkedQueue[A](init.asJava)

  private[controller] def putAndNotify(value: A): Unit = {
    queue.add(value)
    synchronized(notifyAll())
  }

  private def blocking(): A = synchronized {
    Option(queue.poll()) match {
      case None    => wait(); queue.poll()
      case Some(v) => v
    }
  }

  override def run(): Unit = {
    try {
      while (true) {
        onReceive(blocking())
      }
    } catch {
      case _: InterruptedException =>
    }
  }

  def shutdown(): Unit = interrupt()

}
