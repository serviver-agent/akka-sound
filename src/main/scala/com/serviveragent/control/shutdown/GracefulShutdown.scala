package com.serviveragent.control.shutdown

trait GracefulShutdown {

  def receiveStart(): Unit
  def receiveShutdown(): Unit

  protected def gracefulShutdownDispatcher: GracefulShutdownDispatcher
  gracefulShutdownDispatcher.addListener(this)

}
