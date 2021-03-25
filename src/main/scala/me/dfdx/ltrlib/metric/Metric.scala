package me.dfdx.ltrlib.metric

trait Metric {
  def eval(y: Array[Double], yhat: Array[Double]): Double
}
