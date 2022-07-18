package com.serviveragent.control.shutdown

import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*

class GracefulShutdownDispatcher {

  private val logger = LoggerFactory.getLogger(getClass)

  private val listeners = new ConcurrentLinkedQueue[GracefulShutdown]

  private[shutdown] def addListener(listener: GracefulShutdown): Unit = listeners.add(listener)

  def startAll(): Unit = {
    logger.info("start all...")
    listeners.iterator.asScala.foreach { _.receiveStart() }
  }

  def shutdownAll(): Unit = {
    logger.info("shutting down all...")
    listeners.iterator.asScala.foreach { _.receiveShutdown() }
  }

}
