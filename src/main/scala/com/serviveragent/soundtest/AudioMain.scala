package com.serviveragent.soundtest

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import com.serviveragent.controller.{Controller, Subscriber}
import org.slf4j.LoggerFactory

import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine}
import scala.concurrent.duration.*

class AudioMain(
    controller: Controller,
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  private val fs = 44100

  private val audioFormat = new AudioFormat(fs, 24, 1, true, true)
  private val sourceDataLine: SourceDataLine =
    AudioSystem.getSourceDataLine(audioFormat)

  val oscillator: GainSineOscillator = new GainSineOscillator(
    TriangleOscillator(LineOscillator(440), 1.0, fs),
    LineOscillator(0.25)
  )

  val freqSubscriber: Subscriber[Double] =
    controller.freq.getSubscriber("freqSubscriber", oscillator.setFreq(_, 0.05.seconds))
  val ampSubscriber: Subscriber[Double] =
    controller.freq.getSubscriber("ampSubscriber", oscillator.setAmp(_, 0.05.seconds))

  private val iterator: Iterator[Sample] = oscillator.iterator

  private val dest: Array[Byte] = new Array(3 * 1024)

  private var isRunning = false

  private val thread = new Thread {
    override def run(): Unit = {
      sourceDataLine.open()
      sourceDataLine.start()
      while (isRunning) {
        val samples: Array[Sample] = iterator.take(1024).toArray
        SampleConverter.toBytePCM24signBigEndian(samples, dest)
        val bytes = dest.clone
        sourceDataLine.write(bytes, 0, bytes.length)
        controller.generatedSound.publish(samples)
      }
      sourceDataLine.stop()
      sourceDataLine.close()
    }
  }

  override def receiveStart(): Unit = {
    logger.debug("audio start")
    isRunning = true
    thread.start()
    freqSubscriber.start()
    ampSubscriber.start()
  }

  override def receiveShutdown(): Unit = {
    logger.debug("audio shutdown")
    freqSubscriber.shutdown()
    ampSubscriber.shutdown()
    isRunning = false
  }
}
