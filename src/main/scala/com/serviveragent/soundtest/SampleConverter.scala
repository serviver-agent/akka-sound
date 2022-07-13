package com.serviveragent.soundtest

object SampleConverter {

  // Double (-1.0 ~ 1.0) -> PCM 24bit signed big-endian
  // dest is (sample.size * 3) bytes array
  def toBytePCM24signBigEndian(
      samples: Iterable[Sample],
      dest: Array[Byte]
  ): Unit = {
    var offset = 0
    samples.foreach { sample =>
      import sample.value
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

}
