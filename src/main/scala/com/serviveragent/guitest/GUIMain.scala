package com.serviveragent.guitest

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import com.serviveragent.controller.{Controller, Subscriber}
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}
import org.slf4j.LoggerFactory

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics, Graphics2D, Dimension}
import javax.swing.*

class GUIMain(
    controller: Controller,
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  private val frame = new JFrame("Audio Spectrum")
  private val label = new JLabel
  private val image = new BufferedImage(512, 374, BufferedImage.TYPE_INT_RGB)
  private val graphics: Graphics2D = image.createGraphics

  private var isSignalReceiverRunning = false

  private val signalReceiver: Thread = new Thread {
    private val subscriber: Subscriber[Array[Double]] = controller.generatedSound.getSubscriber
    override def run(): Unit = {
      try {
        while (isSignalReceiverRunning) {
          val value = subscriber.blocking()
          fftPaint(value)
        }
      } catch {
        case _: InterruptedException =>
      }
    }
  }

  private val fft = new FastFourierTransformer(DftNormalization.UNITARY)

  private def fftPaint(data: Array[Double]): Unit = {
    val complex = fft.transform(data, TransformType.FORWARD)
    val realHalf: Array[Double] = complex.take(512).map(_.getReal)
    graphics.setColor(Color.BLACK)
    graphics.fillRect(0, 0, 512, 374)
    graphics.setColor(Color.BLUE)
    realHalf.zipWithIndex.foreach { (a, i) =>
      graphics.drawLine(i, 374 - (a / 8.0 * 374).toInt, i, 374) // 8で割っているのは適当である
    }
    label.repaint()
  }

  override def receiveStart(): Unit = SwingUtilities.invokeLater { () =>
    logger.debug("gui start")
    frame.setSize(512, 374 + 187)
    frame.setLocation(100, 100)
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent): Unit = {
        gracefulShutdownDispatcher.shutdownAll()
      }
    })

    frame.add("North", label)
    label.setIcon(new ImageIcon(image))

    val gainControlPanel = new GainControlPanel(0.0)
    gainControlPanel.sliderAddChangeListener { _ =>
      controller.amp.publish(gainControlPanel.getGain)
    }

    frame.add("South", gainControlPanel)

    frame.pack()

    isSignalReceiverRunning = true
    signalReceiver.start()

    frame.setVisible(true)
  }

  override def receiveShutdown(): Unit = {
    logger.debug("gui shutdown")
    isSignalReceiverRunning = false
    frame.setVisible(false)
    frame.dispose()
  }

}
