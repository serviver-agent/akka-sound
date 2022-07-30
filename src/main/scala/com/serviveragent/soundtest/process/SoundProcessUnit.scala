package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import scala.concurrent.duration.*

object SoundProcessUnit {

  case class FreqState(freq: Double)
  def sineGenerator(freq: Double): Generator[Double, FreqState] =
    new Generator(
      name = "",
      initialState = FreqState(freq = freq),
      receive = { (_, _, state, message) =>
        message match {
          case (freq: Double) :: Nil =>
            println(message)
            FreqState(freq = freq)
          case _ => state
        }
      },
      process = { (env, t, state) =>
        val freq = state.freq
        val out = Math.sin(2 * Math.PI * freq * t * env.fsInv)
        (out, state)
      }
    )

  def triangleGenerator(freq: Double): Generator[Double, FreqState] =
    new Generator(
      name = "triangle-gen",
      initialState = FreqState(freq = freq),
      receive = { (_, _, state, message) =>
        message match {
          case (freq: Double) :: Nil =>
            println(message)
            FreqState(freq = freq)
          case _ => state
        }
      },
      process = { (env, t, state) =>
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
  def lineGenerator(initial: Double): Generator[Double, LineState] =
    new Generator(
      name = "line-gen",
      initialState = LineState((_: Long) => initial),
      receive = { (env, t, state, message) =>
        message match {
          case (gain: Double) :: (duration: FiniteDuration) :: Nil =>
            println(message)

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
      process = { (_, t, state) =>
        (state.getAmpFn(t), state)
      }
    )

  val mulProcessor: Processor[(Double, Double), Double, Unit] =
    new Processor(
      name = "",
      initialState = (),
      receive = { case (_, _, _, _) => () },
      process = { case (_, _, (in1, in2), _) =>
        (in1 * in2, ())
      }
    )

  val addProcessor: Processor[(Double, Double), Double, Unit] =
    new Processor(
      name = "",
      initialState = (),
      receive = { case (_, _, _, _) => () },
      process = { case (_, _, (in1, in2), _) =>
        (in1 + in2, ())
      }
    )

  val gen1 = triangleGenerator(440.0)
  val gen2 = lineGenerator(0.5)

  val gainControllableTriangleGenerator: Generator[Sample, (FreqState, LineState)] =
    mul(gen1, gen2)

  def mul[S1, S2](
      gen1: Generator[Sample, S1],
      gen2: Generator[Sample, S2]
  ): Generator[Sample, (S1, S2)] =
    new Generator(
      name = "",
      initialState = (gen1.initialState, gen2.initialState),
      receive = { case (env, t, (s1, s2), mes) =>
        val rs1 = gen1.receive(env, t, s1, mes)
        val rs2 = gen2.receive(env, t, s2, mes)
        (rs1, rs2)
      },
      process = { case (env, t, (s1, s2)) =>
        val (o1, os1) = gen1.process(env, t, s1)
        val (o2, os2) = gen2.process(env, t, s2)
        (o1 * o2, (os1, os2))
      }
    )

  def generatorToProcessorDropInput[In, Out, State](
      gen: Generator[Out, State]
  ): Processor[In, Out, State] = {
    new Processor(
      name = gen.name,
      initialState = gen.initialState,
      receive = gen.receive,
      process = { (env, t, _, state) =>
        gen.process(env, t, state)
      }
    )
  }

}
