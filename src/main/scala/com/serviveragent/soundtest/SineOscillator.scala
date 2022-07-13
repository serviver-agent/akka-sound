package com.serviveragent.soundtest

import scala.util.chaining.*

class SineOscillator(
    var freq: Double,
    var gain: Double,
    var fs: Int,
    var fsInv: Double,
    var t: Int
) {

  def getAmp: Sample =
    Sample(gain * Math.sin(2 * Math.PI * freq * t * fsInv))

  def next(): Unit = t += 1

  def getAmpAndNext(): Sample = getAmp.tap(_ => next())

  def iterator: Iterator[Sample] = new Iterator[Sample] {
    override def hasNext: Boolean = true

    override def next(): Sample = getAmpAndNext()
  }
}

object SineOscillator {

  def apply(freq: Double, gain: Double, fs: Int): SineOscillator =
    new SineOscillator(freq, gain, fs, 1.0 / fs, 0)

}
