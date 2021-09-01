package io.github.metarank.ltrlib.booster

trait Booster {
  def trainOneIteration(): Unit
  def evalMetric(): Double
  def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double]
}

object Booster {
  case class BoosterOptions(trees: Int = 100, learningRate: Double = 0.1)
}
