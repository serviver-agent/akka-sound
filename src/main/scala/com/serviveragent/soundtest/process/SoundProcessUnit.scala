package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import scala.concurrent.duration.*

object SoundProcessUnit {

  case class FreqState(freq: Double)
  def sineGenerator(name: String, freq: Double): Processor[Unit, Double, FreqState] =
    new Processor(
      name = name,
      initialState = FreqState(freq = freq),
      receive = { (_, _, state, message) =>
        message match {
          case (freq: Double) :: Nil => FreqState(freq = freq)
          case _                     => state
        }
      },
      process = { (env, t, _, state) =>
        val freq = state.freq
        val out = Math.sin(2 * Math.PI * freq * t * env.fsInv)
        (out, state)
      }
    )

  def triangleGenerator(name: String, freq: Double): Processor[Unit, Double, FreqState] =
    new Processor(
      name = name,
      initialState = FreqState(freq = freq),
      receive = { (_, _, state, message) =>
        message match {
          case (freq: Double) :: Nil => FreqState(freq = freq)
          case _                     => state
        }
      },
      process = { (env, t, _, state) =>
        // 計算狂ってない？
        val freq = state.freq
        val out = {
          val u = env.fs / freq
          val tmod = t % u.toInt
          if (tmod < u / 4 || 3 * u / 4 <= tmod) {
            4 * tmod / u
          } else {
            -1 * tmod / u + 2
          }
        }
        (out, FreqState(freq))
      }
    )

  case class LineState(getAmpFn: Long => Double)
  def lineGenerator(name: String, initial: Double): Processor[Unit, Double, LineState] =
    new Processor(
      name = name,
      initialState = LineState((_: Long) => initial),
      receive = { (env, t, state, message) =>
        message match {
          case (gain: Double) :: (duration: FiniteDuration) :: Nil =>
            val t0 = t
            val a0 = state.getAmpFn(t)
            val t1 = t0 + (duration.toMillis.toDouble / 1000 * env.fs)
            val a1 = gain
            val slope = (gain - a0) / (t1 - t)
            val getAmpFn = (_t: Long) => {
              if (_t <= t1) {
                slope * (_t - t0) + a0
              } else {
                a1
              }
            }
            LineState(getAmpFn)
          case _ => state
        }
      },
      process = { (_, t, _, state) =>
        (state.getAmpFn(t), state)
      }
    )

}
