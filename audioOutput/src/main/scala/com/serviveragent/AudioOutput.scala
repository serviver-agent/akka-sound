package com.serviveragent.audiooutput

import com.serviveragent.actortest.Signal

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors

import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine}

object AudioOutput:
  def apply(receiveBox: ActorRef[Signal]): Behavior[Signal] =
    audioOutput(receiveBox)

  private def audioOutput(receiveBox: ActorRef[Signal]): Behavior[Signal] =
    Behaviors.receiveMessage { signal =>
      run(signal.value.toByte)
      Behaviors.same
    }

  private val freq = 44100
  private val fmt = new AudioFormat(freq, 8, 1, true, false)
  val audio =
    val a: SourceDataLine = AudioSystem.getSourceDataLine(fmt)
    a.open(fmt)
    a.start()
    a

  def run(signal: Byte): Unit = audio.write(Array(signal), 0, 1)
