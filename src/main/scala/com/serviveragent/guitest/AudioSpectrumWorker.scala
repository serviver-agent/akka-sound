package com.serviveragent.guitest

import com.serviveragent.controller.{Controller, Subscriber}
import com.serviveragent.soundtest.Sample
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}

import java.nio.DoubleBuffer
import java.util
import javax.swing.SwingWorker
import scala.collection.mutable

// コントローラから1024サンプルの信号をsubscribeし、4つ結合して4オーバーラップでFFTを行う。
// すなわち1024サンプルの信号を受信するたびに過去4つの受信結果を利用して4096サンプルのFFTを行う。
// 計算結果はFFTの実部の半分を返すので2048サンプルの配列になる
class AudioSpectrumWorker(
    controller: Controller,
    myProcess: Array[Double] => Unit
) extends SwingWorker[Unit, Array[Double]] {

  import AudioSpectrumWorker.*

  private val subscriber: Subscriber[Array[Sample]] = controller.generatedSound.getSubscriber(onReceive)

  override def doInBackground(): Unit = {
    subscriber.run() // SwingWorkerのスレッドで実行する
  }

  // GUI側のスケジュールによって最後のFFTの実行結果を myProcess によって描画する。
  // 全ての実行結果が描画されるわけではなく、およそ6回のFFTの計算に対して1回しか描画されないようなスケジュールになっている
  // 無駄な計算をしてしまっているので、GUI側のスケジュールを上げたい。
  override def process(chunks: util.List[Array[Sample]]): Unit = {
    myProcess(chunks.get(chunks.size() - 1))
  }

  def shutdown(): Unit = {
    subscriber.shutdown()
  }

  private val fft = new FastFourierTransformer(DftNormalization.UNITARY)

  // 1024サンプルの信号をいくつか格納する。最大4つ格納する想定
  private val arraysQueue: mutable.Queue[Array[Sample]] = new mutable.Queue[Array[Sample]]()
  private val buffer: DoubleBuffer = DoubleBuffer.allocate(1024 * 4)

  private def onReceive(samples: Array[Sample]): Unit = {
    arraysQueue.append(samples) // 最新の1024サンプルをブロッキングして取得、バッファに格納
    if (arraysQueue.size >= 4) {
      arraysQueue.take(4).foreach(arr => buffer.put(arr))
      val joinedSamples: Array[Sample] = buffer.array()
      buffer.clear()
      arraysQueue.dequeue()

      hamming2048inPlace(joinedSamples)
      val complex = fft.transform(joinedSamples, TransformType.FORWARD)
      val realHalf: Array[Double] = complex.take(2048).map(_.getReal)
      publish(realHalf)
    }
  }

}

object AudioSpectrumWorker {

  def hamming2048inPlace(arr: Array[Double]): Unit = {
    (0 until 2048).foreach { t =>
      arr(t) = arr(t) * (0.54 - (0.46 * Math.cos(2 * Math.PI * t / 2048)))
    }
  }

}
