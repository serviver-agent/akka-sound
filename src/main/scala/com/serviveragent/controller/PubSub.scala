package com.serviveragent.controller

import java.util.concurrent.ConcurrentLinkedQueue
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*

class PubSub[A](init: A*) {

  private val subscribers = new ConcurrentLinkedQueue[Subscriber[A]]

  def publish(value: A): Unit = {
    subscribers.iterator.asScala.foreach(_.putAndNotify(value))
  }

  def getSubscriber: Subscriber[A] = {
    new Subscriber[A](init: _*).tap(subscribers.add)
  }

}
