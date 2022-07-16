package com.serviveragent.guitest

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.*
import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import org.slf4j.LoggerFactory

class GUIMain(
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  val frame = new JFrame("Audio Spectrum")

  override def receiveStart(): Unit = SwingUtilities.invokeLater { () =>
    logger.debug("gui start")
    frame.setSize(1024, 768)
    frame.setLocation(200, 200)
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent): Unit = {
        gracefulShutdownDispatcher.shutdownAll()
      }
    })
    frame.setVisible(true)
  }

  override def receiveShutdown(): Unit = {
    logger.debug("gui shutdown")
    frame.setVisible(false)
    frame.dispose()
  }

}
