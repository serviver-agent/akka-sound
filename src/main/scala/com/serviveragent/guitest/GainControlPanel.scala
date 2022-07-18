package com.serviveragent.guitest

import java.awt.{Dimension, FlowLayout}
import javax.swing.*
import javax.swing.event.ChangeListener

class GainControlPanel(
    initialGain: Double
) extends JPanel {

  import GainControlPanel.*

  private val sliderInitial: Int = gainToSliderValue(initialGain)
  private var gain: Double = sliderValueToGain(sliderInitial)

  private val label = new JLabel("gain")
  private val slider = new JSlider(SliderMin, SliderMax, sliderInitial)
  private val sliderValueLabel = new JLabel(labelFormat(gain))

  this.add(label)
  this.add(slider)
  this.add(sliderValueLabel)

  // 指定通りのサイズになっていない。もう少し中央のスライダーを大きくしたいが、
  // 適当に設定するとsliderValueLabelの文字数によってスライダーのサイズが変化してしまうので難しい。
  label.setPreferredSize(new Dimension(64, 32))
  slider.setMinimumSize(new Dimension(384, 32))
  sliderValueLabel.setPreferredSize(new Dimension(64, 32))

  slider.addChangeListener(_ => {
    gain = sliderValueToGain(slider.getValue)
    sliderValueLabel.setText(labelFormat(gain))
  })

  def getGain: Double = gain

  def addGainChangedCallback(fn: Double => Unit): Unit = slider.addChangeListener(_ => {
    fn(gain)
  })

}

object GainControlPanel {

  import GainControlPanel.*

  private val SliderMin: Int = 0
  private val SliderMax: Int = 8192
  private val DivisionPointRatio: Double = 1.0 / 8
  private val DivisionPointDecibelValue: Double = -60.0

  /** スライダーの値 0 ~ 8192 を 音量 0.0 ~ 1.0 にマッピングする
    *
    * その際、スライダーの前半 1/8 (0 ~ 1024) は線形に 0.0 ~ 0.001 (-60dB) にマッピングし、
    * スライダーの後半 1/8 ~ 1 (1024 ~ 8192) は対数上で線形に -60dB ~ 0dB にマッピングする。
    *
    * より一般に、前半部分の比率を DivisionPointRatio, その点における値(dB)を DivisionPointDecibelValue としている。
    */
  private def sliderValueToGain(sliderValue: Int): Double = {
    val ratio: Double = sliderValue.toDouble / SliderMax
    if (ratio <= DivisionPointRatio) {
      ratio / DivisionPointRatio * toLinear(DivisionPointDecibelValue)
    } else {
      toLinear((-DivisionPointDecibelValue) * (1 - DivisionPointRatio) * (ratio - 1))
    }
  }

  private def gainToSliderValue(gain: Double): Int = {
    val ratio: Double = if (gain <= toLinear(DivisionPointDecibelValue)) {
      gain * DivisionPointRatio / toLinear(DivisionPointDecibelValue)
    } else {
      1 + (toDecibel(gain) / ((-DivisionPointDecibelValue) * (1 - DivisionPointRatio)))
    }
    (ratio * SliderMax).toInt
  }

  private def toDecibel(gain: Double): Double = 20 * Math.log10(gain)
  private def toLinear(decibel: Double): Double = Math.pow(10, decibel / 20)

  private def labelFormat(gain: Double): String = {
    val db = toDecibel(gain)
    if (db == Double.NegativeInfinity) "-Inf dB"
    else "%.2f dB".format(db)
  }

}
