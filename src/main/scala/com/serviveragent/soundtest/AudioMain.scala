package com.serviveragent.soundtest

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import org.slf4j.LoggerFactory

import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine}

class AudioMain(
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  val fs = 44100

  val audioFormat = new AudioFormat(fs, 24, 1, true, true)
  val sourceDataLine: SourceDataLine =
    AudioSystem.getSourceDataLine(audioFormat)

  val sineOscillator: SineOscillator = SineOscillator(440, 0.5, fs)
  val sineIterator: Iterator[Sample] = sineOscillator.iterator

  val dest: Array[Byte] = new Array(3 * 1024)

  var isRunning = false

  private val thread = new Thread {
    override def run(): Unit = {
      sourceDataLine.open()
      sourceDataLine.start()
      isRunning = true
      while (isRunning) {
        val samples: Array[Sample] = sineIterator.take(1024).toArray
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

object AudioMain
