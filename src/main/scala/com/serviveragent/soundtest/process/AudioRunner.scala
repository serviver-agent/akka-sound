package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.{Sample, Stereo}
import com.serviveragent.soundtest.process.Graph.*
import com.serviveragent.soundtest.process.Processor.Message
import com.serviveragent.soundtest.process.SoundProcessUnit.{FreqState, LineState}
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable
import scala.concurrent.duration.*

class AudioRunner(env: Environment, graph: Graph) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val queue = new ConcurrentLinkedQueue[(NodeName, Message)]

  def sendMessage(message: (NodeName, Message)): Unit = {
    queue.add(message)
  }

  private def pollAll(): List[(NodeName, Message)] = Iterator.continually(queue.poll()).takeWhile(_ != null).toList

  // -- graph

  val valueMemo: mutable.Map[Node, Option[Sample]] = mutable.HashMap(graph.nodes.map(n => (n, None)): _*)
  val states: mutable.Map[Node, Any] = new mutable.HashMap()

  private[process] def initState(): Unit = {
    states.clear()
    graph.nodes.collect { case n: Node.ProcessorNode => n }.foreach(n => states(n) = n.processor.initialState)
  }
  initState()

  private def getState(node: Node.ProcessorNode): node.processor.S = {
    states(node).asInstanceOf[node.processor.S]
  }
  private def setState(node: Node.ProcessorNode, state: node.processor.S): Unit = {
    states(node) = state
  }

  private def handleMessages(env: Environment, t: Long, args: List[(String, Message)]): Unit = {
    args.foreach { case (nodeName, message) =>
      graph.getNodeByName(nodeName) match {
        case None => logger.warn(s"Node: $nodeName not found. ignored message: ${message.mkString(", ")}")
        case Some(node: Node.ProcessorNode) =>
          val currentState = getState(node)
          val nextState = node.processor.receive(env, t, currentState, message)
          setState(node, nextState)
        case _ => logger.warn(s"Node: $nodeName is not ProcessorNode. it is not controllable.")
      }
    }
  }

  private def process(env: Environment, t: Long, in: Sample): Sample = {
    def proc(node: Node): Sample = {
      val sources = graph.nodesMap(node)
      val sourceValues = sources.map(proc)
      val v: Sample =
        valueMemo(node) match {
          case Some(value) => value
          case None =>
            node match {
              case node: Node.ProcessorNode =>
                val (o, s) = node.processor.process(env, t, (), getState(node))
                setState(node, s)
                o
              case node: Node.SimpleFunctionNode => node.fn(sourceValues)
              case Node.Source                   => in
              case Node.Dest                     => sourceValues.sum
            }
        }
      valueMemo(node) = Some(v)
      v
    }

    val v = proc(Node.Dest)

    valueMemo.keys.foreach(k => valueMemo(k) = None)
    v
  }

  // --- runner

  private var t: Long = 0

  def run(in1: Array[Sample]): Array[Sample] = {
    val out1: Array[Sample] = new Array[Sample](env.blockSize)

    (0 until env.blockSize).foreach { i =>
      val messages = pollAll()
      handleMessages(env, t, messages)
      out1(i) = process(env, t, in1(i))
      t += 1
    }
    out1
  }

}
