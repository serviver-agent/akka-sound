package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample

class Processor[In, Out, State, Message](
    val initialState: State,
    val process: (Environment, Long, In, State, Option[Message]) => (Out, State)
)

object Processor {
  def initialState[S](state: S): AudioUnitBuilder[S] = new AudioUnitBuilder(state)
  class AudioUnitBuilder[S](initialState: S) {
    def process[I, O, M](proc: (Environment, Long, I, S, Option[M]) => (O, S)): Processor[I, O, S, M] =
      new Processor(initialState, proc)
  }
}
