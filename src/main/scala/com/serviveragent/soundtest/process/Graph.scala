package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import com.serviveragent.soundtest.process.Graph.*
import com.serviveragent.soundtest.process.SoundProcessUnit.*

import scala.collection.mutable

class Graph(
    val nodes: Seq[Node],
    val edges: Seq[Edge]
) {

  val nodesMap: Map[Node, Seq[Node]] =
    nodes.map(node => node -> edges.filter(edge => node eq edge.to).map(_.from)).toMap

  val valueMemo: mutable.Map[Node, Option[Sample]] = mutable.HashMap(nodes.map(n => (n, None)): _*)
  val states: mutable.Map[Node, Any] =
    mutable.HashMap(nodes.collect { case n: Node.ProcessorNode => n }.map(n => (n, n.processor.initialState)): _*)

  private def getState(node: Node.ProcessorNode): node.processor.S = {
    states(node).asInstanceOf[node.processor.S]
  }
  private def setState(node: Node.ProcessorNode, state: node.processor.S): Unit = {
    states(node) = state
  }

  def process(env: Environment, t: Long, in: Sample): Sample = {
    def run(node: Node): Sample = {
      val sources = nodesMap(node)
      val sourceValues = sources.map(run)
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

    val v = run(Node.Dest)

    valueMemo.keys.foreach(k => valueMemo(k) = None)
    v
  }

  def graphString: String = {
    s"""graph TD
       |${edges.map(e => s"    ${e.from.name} --> |${e.name}| ${e.to.name}\n").mkString}
       |""".stripMargin
  }

}

object Graph {

  private[process] val triGen = triangleGenerator("triangle-gen", 440.0)
  private[process] val lineGen = lineGenerator("line-gen", 0.5)

  private[process] val mulFn: Seq[Sample] => Sample = _.product
  private[process] val addFn: Seq[Sample] => Sample = _.sum

  sealed trait Node {
    def name: String
  }
  object Node {
    class ProcessorNode(val name: String, val processor: Processor[Unit, Sample, _]) extends Node
    class SimpleFunctionNode(val name: String, val fn: Seq[Sample] => Sample) extends Node
    case object Source extends Node {
      def name = "Source"
    }
    case object Dest extends Node {
      def name = "Dest"
    }
  }
  class Edge(val name: String, val from: Node, val to: Node)

  case class EdgeByName(name: String, from: String, to: String)

  def default: Graph = Graph.create(
    Seq(
      new Node.ProcessorNode("TriangleGenerator", triGen),
      new Node.ProcessorNode("LineGenerator", lineGen),
      new Node.SimpleFunctionNode("mulFn1", mulFn),
      new Node.SimpleFunctionNode("mulFn2", mulFn),
      new Node.SimpleFunctionNode("addFn", addFn)
    ),
    Seq(
      EdgeByName("edge1", "addFn", "Node.Dest"),
      EdgeByName("edge2", "mulFn1", "addFn"),
      EdgeByName("edge3", "TriangleGenerator", "mulFn1"),
      EdgeByName("edge4", "LineGenerator", "mulFn1"),
      EdgeByName("edge5", "mulFn2", "addFn"),
      EdgeByName("edge6", "LineGenerator", "mulFn2"),
      EdgeByName("edge7", "Node.Source", "mulFn2")
    )
  )

  def create(nodes: Seq[Node], edges: Seq[EdgeByName]): Graph = {
    val nodeMap: Map[String, Node] = nodes.map(n => (n.name, n)).toMap
    def resolveNode(nodeName: String): Option[Node] = {
      nodeName match {
        case "Node.Source" => Some(Node.Source)
        case "Node.Dest"   => Some(Node.Dest)
        case _             => nodeMap.get(nodeName)
      }
    }
    val resolvedEdges: Seq[Edge] = edges.map { edge =>
      val from = resolveNode(edge.from)
        .getOrElse(throw new Exception(s"edge: ${edge.name} の from: ${edge.from} に対応するNodeが見つかりません"))
      val to = resolveNode(edge.to)
        .getOrElse(throw new Exception(s"edge: ${edge.name} の to: ${edge.to} に対応するNodeが見つかりません"))
      new Edge(edge.name, from, to)
    }
    new Graph(nodes, resolvedEdges)
  }

}
