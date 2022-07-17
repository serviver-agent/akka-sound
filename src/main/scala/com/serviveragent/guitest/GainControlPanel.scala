package com.serviveragent.guitest

import java.awt.Dimension
import javax.swing.*
import javax.swing.event.ChangeListener

class GainControlPanel(
    initialGain: Double
) extends JPanel {

  import GainControlPanel.*

  val sliderInitial: Int = 0

  private var gain: Double = sliderValueToGain(sliderInitial)

  private val label = new JLabel("gain")
  val slider = new JSlider(SliderMin, SliderMax, sliderInitial)
  private val sliderValueLabel = new JLabel(labelFormat(gain))

  this.add("West", label)
  this.add("Center", slider)
  this.add("East", sliderValueLabel)

  this.setSize(new Dimension(512, 32))
  label.setMaximumSize(new Dimension(64, 32))
  slider.setPreferredSize(new Dimension(384, 32))
  sliderValueLabel.setMaximumSize(new Dimension(64, 32))

  slider.addChangeListener(_ => {
    gain = sliderValueToGain(slider.getValue)
    sliderValueLabel.setText(labelFormat(gain))
  })

  def getGain: Double = gain

  def sliderAddChangeListener(l: ChangeListener): Unit = slider.addChangeListener(l)

}

object GainControlPanel {

  import GainControlPanel.*

  val SliderMin: Int = 0
  val SliderMax: Int = 8192
  val DivisionPointRatio: Double = 1.0 / 8
  val DivisionPointDecibelValue: Double = -60.0

  /** スライダーの値 0 ~ 8192 を 音量 0.0 ~ 1.0 にマッピングする
    *
    * その際、スライダーの前半 1/8 (0 ~ 1024) は線形に 0.0 ~ 0.001 (-60dB) にマッピングし、
    * スライダーの後半 1/8 ~ 1 (1024 ~ 8192) は対数上で線形に -60dB ~ 0dB にマッピングする。
    *
    * より一般に、前半部分の比率を DivisionPointRatio, その点における値(dB)を DivisionPointDecibelValue としている。
    */
  def sliderValueToGain(sliderValue: Int): Double = {
    val ratio: Double = sliderValue.toDouble / SliderMax
    if (ratio <= DivisionPointRatio) {
      ratio / DivisionPointRatio * toLinear(DivisionPointDecibelValue)
    } else {
      toLinear((-DivisionPointDecibelValue) * (1 - DivisionPointRatio) * (ratio - 1))
    }
  }

  def toDecibel(gain: Double): Double = 20 * Math.log10(gain)
  def toLinear(decibel: Double): Double = Math.pow(10, decibel / 20)

  def labelFormat(gain: Double): String = {
    val db = toDecibel(gain)
    if (db == Double.NegativeInfinity) "-Inf dB"
    else "%.2f dB".format(db)
  }

}
