package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import com.serviveragent.soundtest.process.Processor.Message

class Processor[In, Out, State](
    val name: String,
    val initialState: State,
    val receive: (Environment, Long, State, Message) => State,
    val process: (Environment, Long, In, State) => (Out, State)
) {

  type S = State

}

object Processor {

  type Message = List[String]

}
