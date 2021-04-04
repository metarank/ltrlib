package me.dfdx.ltrlib.metric

import io.github.metarank.cfor._

object MSE extends Metric {
  def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double = {
    var sum = 0.0
    var cnt = 0
    cfor(y.indices) { i =>
      cfor(y(i).indices) { j =>
        {
          sum += (y(i)(j) - yhat(i)(j)) * (y(i)(j) - yhat(i)(j))
          cnt += 1
        }
      }

    }
    sum / cnt
  }
}
