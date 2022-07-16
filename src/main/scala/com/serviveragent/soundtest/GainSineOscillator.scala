package com.serviveragent.soundtest

import scala.concurrent.duration.Duration

class GainSineOscillator(
    sineOscillator: SineOscillator,
    lineOscillator: LineOscillator
) {

  def setAmp(v: Double, duration: Duration): Unit = lineOscillator.setAmp(v, duration)

  def getAmpAndNext(): Sample = lineOscillator.getAmpAndNext() * sineOscillator.getAmpAndNext()

  def iterator: Iterator[Sample] = new Iterator[Sample] {
    override def hasNext: Boolean = true
    override def next(): Sample = getAmpAndNext()
  }

}
