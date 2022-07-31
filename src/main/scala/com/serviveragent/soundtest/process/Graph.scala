package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import com.serviveragent.soundtest.process.Graph.*
import com.serviveragent.soundtest.process.SoundProcessUnit.*

import scala.collection.mutable

class Graph private (
    val nodes: Seq[Node],
    val edges: Seq[Edge]
) {

  val nodesMap: Map[Node, Seq[Node]] =
    nodes.map(node => node -> edges.filter(edge => node eq edge.to).map(_.from)).toMap

  private val nodeNameMap: Map[NodeName, Node] = nodes.map(node => (node.name, node)).toMap
  def getNodeByName(name: String): Option[Node] = nodeNameMap.get(name)

  def graphString: String = {
    s"""graph TD
       |${edges.map(e => s"    ${e.from.name} --> |${e.name}| ${e.to.name}\n").mkString}
       |""".stripMargin
  }

}

object Graph {

  type NodeName = String

  sealed trait Node {
    def name: NodeName
  }
  object Node {
    class ProcessorNode(val name: NodeName, val processor: Processor[Unit, Sample, _]) extends Node
    class SimpleFunctionNode(val name: NodeName, val fn: Seq[Sample] => Sample) extends Node
    case object Source extends Node {
      def name: NodeName = "Source"
    }
    case object Dest extends Node {
      def name: NodeName = "Dest"
    }
  }
  class Edge(val name: String, val from: Node, val to: Node)

  case class EdgeByName(name: String, from: NodeName, to: NodeName)

  def create(nodes: Seq[Node], edges: Seq[EdgeByName]): Graph = {

    // nodeやedgeのnameが一意であることを確認

    val duplicatedNameNodes = nodes.groupBy(_.name).filter(_._2.length > 2).keys.toList
    if (duplicatedNameNodes.nonEmpty) {
      throw new Exception(s"Nodeのnameが重複しています: ${duplicatedNameNodes.mkString(", ")}")
    }
    val duplicatedNameEdges = edges.groupBy(_.name).filter(_._2.length > 2).keys.toList
    if (duplicatedNameEdges.nonEmpty) {
      throw new Exception(s"Edgeのnameが重複しています: ${duplicatedNameEdges.mkString(", ")}")
    }

    if (nodes.exists(_.name == "Node.Source")) {
      throw new Exception("Nodeのname Node.Source は予約語で利用できません")
    }
    if (nodes.exists(_.name == "Node.Dest")) {
      throw new Exception("Nodeのname Node.Dest は予約語で利用できません")
    }

    // --

    val nodeMap: Map[NodeName, Node] = nodes.map(n => (n.name, n)).toMap
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
    new Graph(Node.Dest +: Node.Source +: nodes, resolvedEdges)
  }

}
