package me.dfdx.ltrlib.metric

import io.github.metarank.cfor._

object MSE extends Metric {
  def eval(y: Array[Double], yhat: Array[Double]): Double = {
    var sum = 0.0
    cfor(y.indices) { i =>
      {
        sum += (y(i) - yhat(i)) * (y(i) - yhat(i))
      }
    }
    sum / y.length
  }
}
