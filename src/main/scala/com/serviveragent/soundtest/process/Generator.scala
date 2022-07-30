package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample

class Generator[O, S, M](
    val initialState: S,
    val process: (Environment, Long, S, Option[M]) => (O, S)
)
object Generator {
  def initialState[S](state: S): GeneratorBuilder[S] = new GeneratorBuilder(state)
  class GeneratorBuilder[S](initialState: S) {
    def process[O, M](proc: (Environment, Long, S, Option[M]) => (O, S)): Generator[O, S, M] =
      new Generator(initialState, proc)
  }
}
