package com.serviveragent.soundtest

import com.serviveragent.control.shutdown.{GracefulShutdown, GracefulShutdownDispatcher}
import com.serviveragent.controller.{Controller, Subscriber}
import com.serviveragent.soundtest.process.Graph.{EdgeByName, Node}
import com.serviveragent.soundtest.process.parser.GraphParser
import com.serviveragent.soundtest.process.{AudioRunner, Environment, Graph}
import org.slf4j.LoggerFactory

import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine, TargetDataLine}
import scala.concurrent.duration.*

class AudioMain(
    controller: Controller,
    protected val gracefulShutdownDispatcher: GracefulShutdownDispatcher
) extends GracefulShutdown {

  private val logger = LoggerFactory.getLogger(getClass)

  val env: Environment = Environment(44100, 1024)

  private val audioFormat = new AudioFormat(env.fs, 24, 1, true, true)

  private val targetDataLine: TargetDataLine = AudioSystem.getTargetDataLine(audioFormat)

  private val sourceDataLine: SourceDataLine = AudioSystem.getSourceDataLine(audioFormat)

  // -- runner, graph

  val graph: Graph = {
    val str =
      """## nodes
        |
        |* TriangleGen: processor triangle @freq=440.0[20.0, 22050.0]
        |* TriangleGain: processor line @gain=0.5[0.0, 1.0]
        |* triangle: function mul
        |* MicrophoneGain: processor line @gain=0.5[0.0, 1.0]
        |* microphone: function mul
        |* mixed: function add
        |* MasterGain: processor line @gain=0.1[0.0, 1.0]
        |* master: function mul
        |
        |## graph
        |
        |```mermaid
        |graph TD
        |    TriangleGen --> triangle
        |    TriangleGain --> triangle
        |    MicrophoneGain --> microphone
        |    Audio.Source --> microphone
        |    triangle --> mixed
        |    microphone --> mixed
        |    mixed --> master
        |    MasterGain --> master 
        |    master --> Audio.Dest
        |```
        |""".stripMargin
    val graph = GraphParser.toGraph(str).get
    println(graph.graphString)
    graph
  }

  val audioRunner: AudioRunner = new AudioRunner(env, graph)

  val freqSubscriber: Subscriber[Double] =
    controller.freq.getSubscriber { freq =>
      val message = java.math.BigDecimal(freq).toPlainString :: Nil
      audioRunner.sendMessage("TriangleGen", message)
    }
  val ampSubscriber: Subscriber[Double] =
    controller.amp.getSubscriber { amp =>
      val message = java.math.BigDecimal(amp).toPlainString :: "0.05" :: Nil
      audioRunner.sendMessage("MasterGain", message)
    }

  // --

  private val inDest: Array[Byte] = new Array(3 * env.blockSize)
  private val outDest: Array[Byte] = new Array(3 * env.blockSize)

  private var isRunning = false

  private val thread = new Thread {
    override def run(): Unit = {
      targetDataLine.open()
      sourceDataLine.open()
      targetDataLine.start()
      sourceDataLine.start()
      while (isRunning) {
        targetDataLine.read(inDest, 0, inDest.length)
        val inSamples: Array[Sample] = new Array(env.blockSize)
        SampleConverter.fromBytePCM24signBigEndian(inDest, inSamples)
        val processed = audioRunner.run(inSamples)
        SampleConverter.toBytePCM24signBigEndian(processed, outDest)
        val bytes = outDest.clone
        sourceDataLine.write(bytes, 0, bytes.length)
        controller.generatedSound.publish(processed)
      }
      targetDataLine.stop()
      sourceDataLine.stop()
      targetDataLine.close()
      sourceDataLine.close()
    }
  }

  override def receiveStart(): Unit = {
    logger.debug("audio start")
    isRunning = true
    thread.start()
    freqSubscriber.start()
    ampSubscriber.start()
  }

  override def receiveShutdown(): Unit = {
    logger.debug("audio shutdown")
    freqSubscriber.shutdown()
    ampSubscriber.shutdown()
    isRunning = false
  }
}
