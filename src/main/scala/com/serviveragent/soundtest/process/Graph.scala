package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import SoundProcessUnit.*

object Graph {

  val gen1 = triangleGenerator(440.0)
  val gen2 = lineGenerator(0.5)

  val gainControllableTriangleGenerator: Generator[Sample, (FreqState, LineState), Opts[FreqMessage, LineMessage]] =
    mul(gen1, gen2)

  case class Opts[A, B](_1: Option[A], _2: Option[B])

  def mul[S1, M1, S2, M2](
      gen1: Generator[Sample, S1, M1],
      gen2: Generator[Sample, S2, M2]
  ): Generator[Sample, (S1, S2), Opts[M1, M2]] =
    Generator
      .initialState((gen1.initialState, gen2.initialState))
      .process { case (env, t, (s1, s2), mes) =>
        val (o1, os1) = gen1.process(env, t, s1, mes.flatMap(_._1))
        val (o2, os2) = gen2.process(env, t, s2, mes.flatMap(_._2))
        (o1 * o2, (os1, os2))
      }

}
