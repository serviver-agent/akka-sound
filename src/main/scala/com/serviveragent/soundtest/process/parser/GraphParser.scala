package com.serviveragent.soundtest.process.parser

import com.serviveragent.soundtest.process.Graph
import com.serviveragent.soundtest.process.SoundProcessUnit.{
  addFn,
  lineGenerator,
  mulFn,
  sineGenerator,
  triangleGenerator
}

import scala.util.{Failure, Success, Try}

object GraphParser {

  def toGraph(str: String): Try[Graph] = parse(str).flatMap(_.toGraph)

  def parse(str: String): Try[Result] = {
    val lines = str.linesIterator.toList

    val (nodesPart, graphPart) = lines.splitAt(lines.indexWhere(_ == "## graph"))

    val processor = raw"\* ([\w\.]+): processor ([\w\.]+) @(\w+)=([\d\.]+)\[([\d\.]+), ([\d\.]+)\].*".r
    val function = raw"\* ([\w\.]+): function ([\w\.]+).*".r
    val nodes: List[Result.Node] = nodesPart.flatMap { line =>
      line match {
        case processor(nodeName, processorName, argName, argInitial, argMin, argMax)
            if argInitial.toDoubleOption.isDefined && argMin.toDoubleOption.isDefined && argMax.toDoubleOption.isDefined =>
          Some(
            Result.Node
              .Processor(nodeName, processorName, argName, argInitial.toDouble, argMin.toDouble, argMax.toDouble)
          )
        case function(nodeName, functionName) =>
          Some(Result.Node.Function(nodeName, functionName))
        case _ =>
          None
      }
    }

    // edge

    val edgesPart = graphPart
      .drop(graphPart.indexWhere(_.startsWith("```")))
      .drop(2)
      .takeWhile(!_.startsWith("```"))

    val pattern = raw"\s*([\w\.]+)\s*-->\s*([\w\.]+).*".r
    val edges: List[Result.Edge] = edgesPart.flatMap { line =>
      line match {
        case pattern(from, to) =>
          Some(Result.Edge(from, to))
        case _ =>
          None
      }
    }

    Success(Result(nodes, edges))
  }

  case class Result(
      nodes: List[Result.Node],
      edges: List[Result.Edge]
  ) {

    def toGraph: Try[Graph] = Try {
      val gNodes: List[Graph.Node] = nodes.map {
        case Result.Node.Processor(nodeName, processorName, argName, argInitial, argMin, argMax) =>
          val processor = processorName match {
            case "sine"     => sineGenerator("anonymous", argInitial)
            case "triangle" => triangleGenerator("anonymous", argInitial)
            case "line"     => lineGenerator("anonymous", argInitial)
            case _          => throw new Exception(s"processor: $processorName is not found")
          }
          Graph.Node.ProcessorNode(nodeName, processor)
        case Result.Node.Function(nodeName, functionName) =>
          val function = functionName match {
            case "add" => addFn
            case "mul" => mulFn
            case _     => throw new Exception(s"function: $functionName is not found")
          }
          Graph.Node.SimpleFunctionNode(nodeName, function)
      }
      val gEdges: List[Graph.EdgeByName] = edges.map { case Result.Edge(fromName, toName) =>
        Graph.EdgeByName("anonymous", fromName, toName)
      }
      Graph.create(gNodes, gEdges)
    }

  }

  object Result {

    sealed trait Node
    object Node {
      case class Processor(
          nodeName: String,
          processorName: String,
          argName: String,
          argInitial: Double,
          argMin: Double,
          argMax: Double
      ) extends Node
      case class Function(
          nodeName: String,
          functionName: String
      ) extends Node
    }

    case class Edge(fromName: String, toName: String)

  }

}
