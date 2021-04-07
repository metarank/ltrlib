package me.dfdx.ltrlib.booster

trait Booster {
  def trainOneIteration(): Unit
  def evalMetric(): Double
  def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double]
}

object Booster {
  case class BoosterDataset(data: Array[Double], labels: Array[Double], groups: Array[Int], rows: Int, cols: Int)
  case class BoosterOptions(trees: Int)
}
