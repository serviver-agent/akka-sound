package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample

class Generator[Out, State](
    val name: String,
    val initialState: State,
    val receive: (Environment, Long, State, List[Any]) => State,
    val process: (Environment, Long, State) => (Out, State)
) {

  def receiveMessages(env: Environment, t: Long, state: State, messages: List[(String, List[Any])]): State = {
    val messagesForMe: List[List[Any]] = messages.filter(_._1 == name).map(_._2)
    messagesForMe.foldLeft(state)((s, mes) => receive(env, t, s, mes))
  }

}
