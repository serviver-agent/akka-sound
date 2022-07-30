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

  val mulFn: (Sample, Sample) => Sample = _ * _
  val addFn: (Sample, Sample) => Sample = _ * _

  val triNode = new Node.GeneratorNode(triGen)
  val lineNode = new Node.GeneratorNode(lineGen)
  val mulNode1 = new Node.SimpleFunctionNode(mulFn)
  val mulNode2 = new Node.SimpleFunctionNode(mulFn)
  val addNode = new Node.SimpleFunctionNode(addFn)

  val edge1 = new Edge(addNode, Node.Dest)
  val edge2 = new Edge(mulNode1, addNode)
  val edge3 = new Edge(triNode, mulNode1)
  val edge4 = new Edge(lineNode, mulNode1)
  val edge5 = new Edge(mulNode2, addNode)
  val edge6 = new Edge(lineNode, mulNode2)
  val edge7 = new Edge(Node.Source, mulNode2)

  val edges: List[Edge] = List(edge1, edge2, edge3, edge4, edge5, edge6, edge7)
  val nodes: List[Node] = List(Node.Source, Node.Dest, triNode, lineNode, mulNode1, mulNode2, addNode)
  val nodesMap: Map[Node, List[Node]] =
    nodes.map(node => node -> edges.filter(edge => node eq edge.to).map(_.from)).toMap

  val valueMemo: mutable.Map[Node, Option[Sample]] = mutable.HashMap(nodes.map(n => (n, None)): _*)
  val states: mutable.Map[Node, Any] =
    mutable.HashMap(nodes.collect { case n: Node.GeneratorNode => n }.map(n => (n, n.processor.initialState)): _*)

  def process(env: Environment, t: Long, in: Sample): Sample = {
    def run(node: Node): Sample = {
      val sources = nodesMap(node)
      val sourceValues = sources.map(run)
      val v: Sample =
        valueMemo(node) match {
          case Some(value) => value
          case None =>
            node match {
              case node: Node.GeneratorNode =>
                val (o, s) = node.processor.process(env, t, (), states(node).asInstanceOf[node.processor.S])
                states(node) = s.asInstanceOf[node.processor.S]
                o
              case node: Node.SimpleFunctionNode => node.fn(sourceValues(0), sourceValues(1)) // うーん
              case Node.Source                   => in
              case Node.Dest                     => sourceValues.sum
            }
        }
      valueMemo(node) = Some(v)
      v
    }

    val v = run(Node.Dest)

    println(valueMemo)

    valueMemo.keys.foreach(k => valueMemo(k) = None)
    v
  }
}

object Graph {

  sealed trait Node
  object Node {
    class GeneratorNode(val processor: Processor[Unit, Sample, _]) extends Node
    class SimpleFunctionNode(val fn: (Sample, Sample) => Sample) extends Node
    case object Source extends Node
    case object Dest extends Node
  }
  class Edge(val from: Node, val to: Node)

}
