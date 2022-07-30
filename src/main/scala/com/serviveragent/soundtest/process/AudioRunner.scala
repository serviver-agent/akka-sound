package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.{Sample, Stereo}
import com.serviveragent.soundtest.process.SoundProcessUnit.*

import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.duration.*

class AudioRunner(env: Environment) {

  private val queue = new ConcurrentLinkedQueue[(String, List[Any])]

  def sendMessage(message: (String, List[Any])): Unit = {
    queue.add(message)
  }

  def pollAll(): List[(String, List[Any])] = Iterator.continually(queue.poll()).takeWhile(_ != null).toList

  private var t: Long = 0

  private val processor: Processor[Sample, Sample, (FreqState, LineState)] =
    dropInput(gainControllableTriangleGenerator)

  private var state = processor.initialState

  def run(in1: Array[Sample]): Array[Sample] = {
    val out1: Array[Sample] = new Array[Sample](env.blockSize)

    (0 until env.blockSize).foreach { i =>
      val messages = pollAll()
      state = processor.receiveMessages(env, t, state, messages)
      val (o1, s) = processor.process(env, t, in1(i), state)
      out1(i) = o1
      state = s
      t += 1
    }
    out1
  }

}
