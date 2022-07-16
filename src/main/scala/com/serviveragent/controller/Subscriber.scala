package com.serviveragent.controller

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*

class Subscriber[A](init: A*) {

  private val queue = new ConcurrentLinkedQueue[A](init.asJava)

  private[controller] def putAndNotify(value: A): Unit = {
    queue.add(value)
    synchronized(notifyAll())
  }

  def blocking(): A = synchronized {
    Option(queue.poll()) match {
      case None    => wait(); queue.poll()
      case Some(v) => v
    }
  }

}
