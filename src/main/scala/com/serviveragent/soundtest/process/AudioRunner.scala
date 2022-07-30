package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import Graph.gainControllableTriangleGenerator

class AudioRunner(env: Environment) {

  private var t: Long = 0

  private var state = gainControllableTriangleGenerator.initialState

  def run(in: Array[Sample]): Array[Sample] = {
    val out: Array[Sample] = new Array[Sample](env.blockSize)
    (0 until env.blockSize).foreach { i =>
      val (o, s) = Graph.gainControllableTriangleGenerator.process(env, t, state, None)
      out(i) = o
      state = s
      t += 1
    }
    out
  }

}
