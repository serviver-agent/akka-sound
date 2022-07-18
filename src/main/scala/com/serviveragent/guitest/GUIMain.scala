package com.serviveragent.guitest

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import com.serviveragent.controller.{Controller, Subscriber}
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}
import org.slf4j.LoggerFactory

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics, Graphics2D, Image}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import javax.swing.*

class GUIMain(
    controller: Controller,
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  private val frame = new JFrame("Audio Spectrum")
  private val label = new JLabel
  private val image = new BufferedImage(2048, 1496, BufferedImage.TYPE_INT_RGB)
  private val graphics: Graphics2D = image.createGraphics

  val audioSpectrumWorker = new AudioSpectrumWorker(controller, fftPaint)

  private def fftPaint(absHalf: Array[Double]): Unit = {
    graphics.setColor(Color.BLACK)
    graphics.fillRect(0, 0, 2048, 1496)
    graphics.setColor(Color.BLUE)
    absHalf.zipWithIndex.foreach { (a, i) =>
      val power = 20 * Math.log10(a) // 負の数(単位はdB)になる
      // 20dB ~ -80dBまで表示する
      val f = i / 2048.0 * 44100.0 / 2
      val xpos = if (f <= 0.0) {
        0
      } else {
        // Math.log10(22010) ==  4.34262004255
        (Math.log10(f) / 4.34262004255 * 2048).toInt
      }

      graphics.drawLine(xpos, (1496 * (power - 20) / -100).toInt, xpos, 1496)
    }
    label.setIcon(new ImageIcon(image.getScaledInstance(1024, 748, Image.SCALE_FAST)))
    label.repaint()
  }

  override def receiveStart(): Unit = SwingUtilities.invokeLater { () =>
    logger.debug("gui start")
    frame.setSize(1024, 748 + 187)
    frame.setLocation(100, 100)
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent): Unit = {
        gracefulShutdownDispatcher.shutdownAll()
      }
    })

    label.setIcon(new ImageIcon(image.getScaledInstance(1024, 748, Image.SCALE_FAST)))
    frame.add("North", label)

    val gainControlPanel = new GainControlPanel(0.5)
    gainControlPanel.addGainChangedCallback(controller.amp.publish)

    frame.add("South", gainControlPanel)
    frame.pack()

    audioSpectrumWorker.execute()
    frame.setVisible(true)
  }

  override def receiveShutdown(): Unit = {
    logger.debug("gui shutdown")
    audioSpectrumWorker.shutdown()
    frame.setVisible(false)
    frame.dispose()
  }

}
