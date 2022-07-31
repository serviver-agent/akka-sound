package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample

class Processor[In, Out, State](
    val name: String,
    val initialState: State,
    val receive: (Environment, Long, State, List[Any]) => State,
    val process: (Environment, Long, In, State) => (Out, State)
) {

  type S = State

  def receiveMessages(env: Environment, t: Long, state: State, messages: List[(String, List[Any])]): State = {
//    val messagesForMe: List[List[Any]] = messages.filter(_._1 == name).map(_._2)
    val messagesForMe: List[List[Any]] = messages.map(_._2)

    messagesForMe.foldLeft(state)((s, mes) => receive(env, t, s, mes))
  }

}