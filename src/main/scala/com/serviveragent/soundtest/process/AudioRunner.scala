package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.{Sample, Stereo}
import com.serviveragent.soundtest.process.Graph.*
import com.serviveragent.soundtest.process.SoundProcessUnit.{FreqState, LineState}

import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.duration.*

class AudioRunner(env: Environment) {

  private val queue = new ConcurrentLinkedQueue[(String, List[Any])]

  def sendMessage(message: (String, List[Any])): Unit = {
    queue.add(message)
  }

  def pollAll(): List[(String, List[Any])] = Iterator.continually(queue.poll()).takeWhile(_ != null).toList

  private var t: Long = 0

  val graph = new Graph

  def run(in1: Array[Sample]): Array[Sample] = {
    val out1: Array[Sample] = new Array[Sample](env.blockSize)

    (0 until env.blockSize).foreach { i =>
      val messages = pollAll()
      messages.foreach(println)
//      state = processor.receiveMessages(env, t, state, messages) // FIXME
      out1(i) = graph.process(env, t, in1(i))
      t += 1
    }
    out1
  }

}
