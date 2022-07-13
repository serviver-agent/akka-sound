package com.serviveragent.soundtest

import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine}

class AudioMain extends Runnable:

  val fs = 44100

  val audioFormat = new AudioFormat(fs, 24, 1, true, true)
  val sourceDataLine: SourceDataLine = AudioSystem.getSourceDataLine(audioFormat)

  val sineOscillator: SineOscillator = SineOscillator(440, 0.5, fs)
  val sineIterator: Iterator[Sample] = sineOscillator.iterator

  val dest: Array[Byte] = new Array(3 * 1024)

  var isRunning = false

  def run(): Unit =
    println("heyheys")
    sourceDataLine.open()
    sourceDataLine.start()
    isRunning = true
    while isRunning do
      val samples: Array[Sample] = sineIterator.take(1024).toArray
      SampleConverter.toBytePCM24signBigEndian(samples, dest)
      val bytes = dest.clone
      sourceDataLine.write(bytes, 0, bytes.length)
    sourceDataLine.stop()
    sourceDataLine.close()
    println("closed")

  def stop(): Unit = isRunning = false

object AudioMain
