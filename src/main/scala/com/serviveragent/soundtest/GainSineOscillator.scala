package com.serviveragent.soundtest

import scala.concurrent.duration.Duration

class GainSineOscillator(
    oscillator: TriangleOscillator,
    lineOscillator: LineOscillator
) {

  def setFreq(freq: Double, duration: Duration): Unit = oscillator.setFreq(freq, duration)
  def setAmp(v: Double, duration: Duration): Unit = lineOscillator.setAmp(v, duration)

  def getAmpAndNext(): Sample = lineOscillator.getAmpAndNext() * oscillator.getAmpAndNext()

  def iterator: Iterator[Sample] = new Iterator[Sample] {
    override def hasNext: Boolean = true
    override def next(): Sample = getAmpAndNext()
  }

}
