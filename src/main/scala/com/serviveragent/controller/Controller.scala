package com.serviveragent.controller

import com.serviveragent.soundtest.Sample

import java.util.concurrent.atomic.AtomicReference

class Controller {

  val amp: PubSub[Double] = new PubSub()
  val freq: PubSub[Double] = new PubSub()

  // 1024サンプル単位で送る
  val generatedSound: PubSub[Array[Sample]] = new PubSub()

}
