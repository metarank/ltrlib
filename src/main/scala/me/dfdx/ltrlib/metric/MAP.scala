package me.dfdx.ltrlib.metric

import io.github.metarank.cfor.cfor

case class MAP(cutoff: Int) extends Metric {
  override def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double = {
    var precision = 0.0
    cfor(y.indices) { group =>
      {
        val zipped   = y(group).zip(yhat(group)).sortBy(-_._2).take(cutoff)
        val relevant = zipped.count(_._1 > 0.0)
        precision += relevant.toDouble / cutoff
      }
    }
    precision / y.length
  }
}
