package com.serviveragent.soundtest.process

case class Environment(fs: Int, blockSize: Int) {
  val fsInv: Double = 1.0 / fs
}
