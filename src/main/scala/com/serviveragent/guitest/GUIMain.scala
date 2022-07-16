package com.serviveragent.guitest

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.*
import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import com.serviveragent.controller.Controller
import org.slf4j.LoggerFactory

import java.awt.{Graphics, Graphics2D}
import java.awt.image.BufferedImage

class GUIMain(
    controller: Controller,
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  val frame = new JFrame("Audio Spectrum")

  override def receiveStart(): Unit = SwingUtilities.invokeLater { () =>
    logger.debug("gui start")
    frame.setSize(512, 374)
    frame.setLocation(200, 200)
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent): Unit = {
        gracefulShutdownDispatcher.shutdownAll()
      }
    })

    val label = new JLabel
    frame.add(label)
    val image = new BufferedImage(512, 374, BufferedImage.TYPE_INT_RGB)
    label.setIcon(new ImageIcon(image))
    frame.pack
    drawGraphics(image.createGraphics)
    label.repaint()

    frame.setVisible(true)
  }

  override def receiveShutdown(): Unit = {
    logger.debug("gui shutdown")
    frame.setVisible(false)
    frame.dispose()
  }

  def drawGraphics(g: Graphics): Unit = {
    import com.serviveragent.soundtest.Sample
    import com.serviveragent.soundtest.SineOscillator
    import org.apache.commons.math3.complex.Complex
    import org.apache.commons.math3.transform.{TransformType, DftNormalization, FastFourierTransformer}

    val fft = new FastFourierTransformer(DftNormalization.UNITARY)
    val data: Array[Sample] = {
      (SineOscillator(4400, 0.5, 44100).iterator zip SineOscillator(440, 0.5, 44100).iterator)
        .map(_ + _)
        .take(1024)
        .toArray
    }
    val complex = fft.transform(data, TransformType.FORWARD)
    val realHalf: Array[Double] = complex.take(512).map(_.getReal)
    realHalf.zipWithIndex.foreach { (a, i) =>
      g.drawLine(i, 374 - (a / 8.0 * 374).toInt, i, 374)
    }
//    complex.foreach(c => println(s"${c.getReal}, ${c.getImaginary}"))

//    g.drawLine(0, 0, 1024, 768)
  }

}
