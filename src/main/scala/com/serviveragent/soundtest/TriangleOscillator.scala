package com.serviveragent.soundtest

import scala.concurrent.duration.Duration
import scala.util.chaining.*

class TriangleOscillator(
    freqLine: LineOscillator,
    var gain: Double,
    var fs: Int,
    var t: Int
) {

  def setFreq(freq: Double, duration: Duration): Unit = {
    freqLine.setAmp(freq, duration)
  }

  def freq = freqLine.getAmp

  def getAmp: Sample = {
    val u = fs / freq
    val tmod = t % u.toInt
    val a = if (tmod < u / 4 || 3 * u / 4 <= tmod) {
      4 * tmod / u
    } else {
      -1 * tmod / u + 2
    }
    gain * a
  }

  def next(): Unit = {
    t += 1
    freqLine.next()
  }

  def getAmpAndNext(): Sample = getAmp.tap(_ => next())

  def iterator: Iterator[Sample] = new Iterator[Sample] {
    override def hasNext: Boolean = true

    override def next(): Sample = getAmpAndNext()
  }
}

object TriangleOscillator {
  def apply(freqLine: LineOscillator, gain: Double, fs: Int): TriangleOscillator =
    new TriangleOscillator(freqLine, gain, fs, 0)
}
