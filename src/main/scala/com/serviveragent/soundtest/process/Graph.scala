package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import com.serviveragent.soundtest.process.Graph.{Edge, Node}
import com.serviveragent.soundtest.process.SoundProcessUnit.{
  FreqState,
  LineState,
  lineGenerator,
  sineGenerator,
  triangleGenerator
}

import scala.collection.mutable

class Graph {

  val triGen = triangleGenerator("triangle-gen", 440.0)
  val lineGen = lineGenerator("line-gen", 0.5)

  val mulFn: Seq[Sample] => Sample = _.product
  val addFn: Seq[Sample] => Sample = _.sum

  val triNode = new Node.ProcessorNode("TriangleGenerator", triGen)
  val lineNode = new Node.ProcessorNode("LineGenerator", lineGen)
  val mulNode1 = new Node.SimpleFunctionNode("mulFn1", mulFn)
  val mulNode2 = new Node.SimpleFunctionNode("mulFn2", mulFn)
  val addNode = new Node.SimpleFunctionNode("addFn", addFn)

  val edge1 = new Edge("edge1", addNode, Node.Dest)
  val edge2 = new Edge("edge2", mulNode1, addNode)
  val edge3 = new Edge("edge3", triNode, mulNode1)
  val edge4 = new Edge("edge4", lineNode, mulNode1)
  val edge5 = new Edge("edge5", mulNode2, addNode)
  val edge6 = new Edge("edge6", lineNode, mulNode2)
  val edge7 = new Edge("edge7", Node.Source, mulNode2)

  val edges: List[Edge] = List(edge1, edge2, edge3, edge4, edge5, edge6, edge7)
  val nodes: List[Node] = List(Node.Source, Node.Dest, triNode, lineNode, mulNode1, mulNode2, addNode)
  val nodesMap: Map[Node, List[Node]] =
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

}
