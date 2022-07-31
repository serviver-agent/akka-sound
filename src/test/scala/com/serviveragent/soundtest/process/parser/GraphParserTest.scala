package com.serviveragent.soundtest.process.parser

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import GraphParser.*

class GraphParserTest extends AnyFlatSpec with EitherValues {

  it should "parse1" in {
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
        |    mixed --> MasterGain
        |    MasterGain --> Audio.Dest
        |```
        |""".stripMargin
    val result = GraphParser.parse(str)

    val expect = Result(
      nodes = List(
        Result.Node.Processor("TriangleGen", "triangle", "freq", 440.0, 20.0, 22050.0),
        Result.Node.Processor("TriangleGain", "line", "gain", 0.5, 0.0, 1.0),
        Result.Node.Function("triangle", "mul"),
        Result.Node.Processor("MicrophoneGain", "line", "gain", 0.5, 0.0, 1.0),
        Result.Node.Function("microphone", "mul"),
        Result.Node.Function("mixed", "add"),
        Result.Node.Processor("MasterGain", "line", "gain", 0.1, 0.0, 1.0)
      ),
      edges = List(
        Result.Edge("TriangleGen", "triangle"),
        Result.Edge("TriangleGain", "triangle"),
        Result.Edge("MicrophoneGain", "microphone"),
        Result.Edge("Audio.Source", "microphone"),
        Result.Edge("triangle", "mixed"),
        Result.Edge("microphone", "mixed"),
        Result.Edge("mixed", "MasterGain"),
        Result.Edge("MasterGain", "Audio.Dest")
      )
    )

    assert(result.toEither.value == expect)
  }

}
