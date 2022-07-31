package com.serviveragent.soundtest.process

import com.serviveragent.soundtest.Sample
import com.serviveragent.soundtest.process.Graph.{EdgeByName, Node, addFn, lineGen, mulFn, triGen}
import com.serviveragent.soundtest.process.SoundProcessUnit.{lineGenerator, triangleGenerator, sineGenerator}
import org.scalatest.flatspec.AnyFlatSpec

class GraphTest extends AnyFlatSpec {

  val env = Environment(100, 1024)

  "default graph" should "aaa" in {
    val triGen = triangleGenerator("triangle-gen", 4)
    val lineGen = lineGenerator("line-gen", 0.5)
    val mulFn: Seq[Sample] => Sample = _.product
    val addFn: Seq[Sample] => Sample = _.sum

    val graph = Graph.create(
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

    (0 until 100).foreach { t =>
      println(s"$t, ${graph.process(env, t = t, in = 0)}")
    }

    succeed
  }

}
