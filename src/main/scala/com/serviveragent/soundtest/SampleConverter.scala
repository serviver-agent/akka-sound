package com.serviveragent.soundtest

object SampleConverter:

  // Double (-1.0 ~ 1.0) -> PCM 24bit signed big-endian
  // dest is 3 bytes array
  def toBytePCM24signBigEndian(sample: Sample, dest: Array[Byte]): Unit =
    import sample.value
    if value >= 1.0 then
      dest(0) = 127; dest(1) = -1; dest(2) = -1 // 01111111 11111111 11111111
    else if value < -1.0 then
      dest(0) = -128; dest(1) = 0; dest(2) = 0 //  10000000 00000000 00000000
    else
      val x = (value * 0x800000).toInt
      dest(0) = (x >>> 16).asInstanceOf[Byte]
      dest(1) = (x >>> 8).asInstanceOf[Byte]
      dest(2) = x.asInstanceOf[Byte]

  // Double (-1.0 ~ 1.0) -> PCM 24bit signed big-endian
  // dest is (sample.size * 3) bytes array
  def toBytePCM24signBigEndian(
      samples: Iterable[Sample],
      dest: Array[Byte]
  ): Unit =
    samples.zipWithIndex.foreach { case (sample, i) =>
      import sample.value
      if value >= 1.0 then
        // 01111111 11111111 11111111
        dest(i + 0) = 127; dest(i + 1) = -1; dest(i + 2) = -1
      else if value < -1.0 then
        // 10000000 00000000 00000000
        dest(i + 0) = -128; dest(i + 1) = 0; dest(i + 2) = 0
      else
        val x = (value * 0x800000).toInt
        dest(i + 0) = (x >>> 16).asInstanceOf[Byte]
        dest(i + 1) = (x >>> 8).asInstanceOf[Byte]
        dest(i + 2) = x.asInstanceOf[Byte]
    }
