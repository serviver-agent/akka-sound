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

  val freqReceiver: Thread = new Thread {
    private val subscriber: Subscriber[Double] = controller.freq.getSubscriber
    override def run(): Unit = {
      try {
        while (isRunning) {
          val value = subscriber.blocking()
          oscillator.setFreq(value, 0.05.seconds)
        }
      } catch {
        case _: InterruptedException =>
      }
    }
  }

  val ampReceiver: Thread = new Thread {
    private val subscriber: Subscriber[Double] = controller.amp.getSubscriber
    override def run(): Unit = {
      try {
        while (isRunning) {
          val value = subscriber.blocking()
          oscillator.setAmp(value, 0.05.seconds)
        }
      } catch {
        case _: InterruptedException =>
      }
    }
  }

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
    freqReceiver.start()
    ampReceiver.start()
  }

  override def receiveShutdown(): Unit = {
    logger.debug("audio shutdown")
    freqReceiver.interrupt()
    ampReceiver.interrupt()
    isRunning = false
  }
}
