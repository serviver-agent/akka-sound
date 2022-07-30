package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import scala.concurrent.duration.*

object SoundProcessUnit {

  case class FreqState(freq: Double)
  case class FreqMessage(freq: Double)
  def sineGenerator(freq: Double): Generator[Double, FreqState, FreqMessage] =
    Generator
      .initialState(FreqState(freq = freq))
      .process { (env, t, state, message) =>
        val freq = message.map(_.freq).getOrElse(state.freq)
        val out = Math.sin(2 * Math.PI * freq * t * env.fsInv)
        (out, FreqState(freq))
      }

  def triangleGenerator(freq: Double): Generator[Double, FreqState, FreqMessage] =
    Generator
      .initialState(FreqState(freq = freq))
      .process { (env, t, state, message) =>
        val freq = message.map(_.freq).getOrElse(state.freq)
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

  case class LineState(getAmpFn: Long => Double)
  case class LineMessage(newValue: Double, duration: FiniteDuration)
  def lineGenerator(initial: Double): Generator[Double, LineState, LineMessage] =
    Generator
      .initialState(LineState((_: Long) => initial))
      .process { (env, t, state, message) =>
        val fn = message match {
          case Some(LineMessage(newValue, duration)) =>
            val t0 = t
            val a0 = state.getAmpFn(t)
            val t1 = t0 + (duration.toMillis.toDouble / 1000 * env.fs)
            val a1 = newValue
            val slope = (newValue - a0) / (t1 - t)
            (_t: Long) => {
              if (_t <= t1) {
                slope * (_t - t0) + a0
              } else {
                a1
              }
            }
          case None => state.getAmpFn
        }
        (fn(t), LineState(fn))
      }

  val mulProcessor: Processor[(Double, Double), Double, Unit, Unit] =
    Processor
      .initialState(())
      .process { case (_, _, (in1, in2), _, _) =>
        (in1 * in2, ())
      }

  val addProcessor: Processor[(Double, Double), Double, Unit, Unit] =
    Processor
      .initialState(())
      .process { case (_, _, (in1, in2), _, _) =>
        (in1 + in2, ())
      }

}
