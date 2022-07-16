package com.serviveragent

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import com.serviveragent.soundtest.{AudioControl, AudioMain}
import org.slf4j.LoggerFactory

import java.io.{BufferedReader, IOException, InputStream, InputStreamReader}

class ConsoleMain(
    inputStream: InputStream,
    audioControl: AudioControl,
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  private val reader = new BufferedReader(new InputStreamReader(inputStream))

  private var isRunning = true

  private val thread: Thread = new Thread {
    override def run(): Unit = {
      while (isRunning) {
        if (reader.ready()) {
          val command = reader.readLine()
          command match {
            case "quit" => gracefulShutdownDispatcher.shutdownAll()
            case v if v.toDoubleOption.isDefined =>
              println(s"set amp: $v")
              audioControl.setAmp(v.toDouble)
            case _ => println(s"receive command: $command")
          }
        } else {
          try {
            Thread.sleep(50)
          } catch {
            case _: InterruptedException =>
          }
        }
      }
    }
  }

  override def receiveStart(): Unit = {
    logger.debug("console start")
    thread.start()
  }

  override def receiveShutdown(): Unit = {
    logger.debug("console shutdown")
    isRunning = false
    thread.interrupt()
    reader.close()
  }

}
