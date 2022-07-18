package com.serviveragent.soundtest

import scala.concurrent.duration.Duration
import scala.util.chaining.*

class LineOscillator(initial: Double) {

  private var t: Int = 0
  private var getAmpFn: Int => Double = _ => initial

  def setAmp(v: Double, duration: Duration): Unit = {
    val t0 = t
    val a0 = getAmp
    val t1 = t0 + (duration.toMillis.toDouble / 1000 * 44100)
    val a1 = v
    val slope = (v - a0) / (t1 - t)
    getAmpFn = (_t: Int) => {
      if (_t <= t1) {
        slope * (_t - t0) + a0
      } else {
        a1
      }
    }
  }

  def getAmp: Sample = getAmpFn(t)

  def next(): Unit = t += 1

  def getAmpAndNext(): Sample = getAmp.tap(_ => next())

  def iterator: Iterator[Sample] = new Iterator[Sample] {
    override def hasNext: Boolean = true
    override def next(): Sample = getAmpAndNext()
  }
}
