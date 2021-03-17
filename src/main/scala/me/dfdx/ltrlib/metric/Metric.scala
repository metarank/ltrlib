package me.dfdx.ltrlib.metric

trait Metric {
  def eval(y: Double, yhat: Double): Double
  def gradient(y: Double, yhat: Double): Double
}
