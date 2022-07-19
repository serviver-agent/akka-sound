package com.serviveragent.soundtest

import java.nio.ByteBuffer

object SampleConverter {

  // Double (-1.0 ~ 1.0) -> PCM 24bit signed big-endian
  // dest is (sample.size * 3) bytes array
  def toBytePCM24signBigEndian(
      samples: Iterable[Sample],
      dest: Array[Byte]
  ): Unit = {
    var offset = 0
    samples.foreach { value =>
      if (value >= 1.0) {
        // 01111111 11111111 11111111
        dest(offset) = 127; offset += 1
        dest(offset) = -1; offset += 1
        dest(offset) = -1; offset += 1
      } else if (value < -1.0) {
        // 10000000 00000000 00000000
        dest(offset) = -128; offset += 1
        dest(offset) = 0; offset += 1
        dest(offset) = 0; offset += 1
      } else {
        val x = (value * 0x800000).toInt
        dest(offset) = (x >>> 16).asInstanceOf[Byte]; offset += 1
        dest(offset) = (x >>> 8).asInstanceOf[Byte]; offset += 1
        dest(offset) = x.asInstanceOf[Byte]; offset += 1
      }
    }
  }

  def fromBytePCM24signBigEndian(
      bytes: Iterable[Byte],
      dest: Array[Sample]
  ): Unit = {
    val buffer: ByteBuffer = ByteBuffer.allocate(4)
    bytes.grouped(3).zipWithIndex.foreach { case (bs, i) =>
      val Array(a, b, c) = bs.toArray
      val msb: Byte = if ((a & 0x80) == 0x80) 0xff.asInstanceOf[Byte] else 0x00.asInstanceOf[Byte]
      buffer.put(0, msb)
      buffer.put(1, a)
      buffer.put(2, b)
      buffer.put(3, c)
      val intValue = buffer.getInt(0)
      dest(i) = intValue.toDouble / 0x800000
    }
  }

}
