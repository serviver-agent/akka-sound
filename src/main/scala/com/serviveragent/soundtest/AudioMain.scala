package com.serviveragent.soundtest

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import org.slf4j.LoggerFactory

import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine}
import scala.concurrent.duration.*

class AudioMain(
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends AudioControl
    with GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  private val fs = 44100

  private val audioFormat = new AudioFormat(fs, 24, 1, true, true)
  private val sourceDataLine: SourceDataLine =
    AudioSystem.getSourceDataLine(audioFormat)

  val oscillator: GainSineOscillator = new GainSineOscillator(
    SineOscillator(440, 1.0, fs),
    LineOscillator(0.5)
  )
  override def setAmp(value: Double): Unit = oscillator.setAmp(value, 0.05.seconds)
  private val iterator: Iterator[Sample] = oscillator.iterator

  private val dest: Array[Byte] = new Array(3 * 1024)

  private var isRunning = false

  private val thread = new Thread {
    override def run(): Unit = {
      sourceDataLine.open()
      sourceDataLine.start()
      isRunning = true
      while (isRunning) {
        val samples: Array[Sample] = iterator.take(1024).toArray
        SampleConverter.toBytePCM24signBigEndian(samples, dest)
        val bytes = dest.clone
        sourceDataLine.write(bytes, 0, bytes.length)
      }
      sourceDataLine.stop()
      sourceDataLine.close()
    }
  }

  override def receiveStart(): Unit = {
    logger.debug("audio start")
    thread.start()
  }

  override def receiveShutdown(): Unit = {
    logger.debug("audio shutdown")
    isRunning = false
  }
}
