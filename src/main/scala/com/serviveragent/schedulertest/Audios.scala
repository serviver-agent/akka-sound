package com.serviveragent.schedulertest

import javax.sound.sampled.*

object Audios:

  val audioFormat = new AudioFormat(44100, 8, 1, true, false)
  val sourceDataLine: SourceDataLine = AudioSystem.getSourceDataLine(audioFormat)
  sourceDataLine.open()
  sourceDataLine.start()
