package io.github.metarank.ltrlib.metric

object MRR extends Metric {
  override def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double = {
    var mrr = 0.0
    for {
      group <- y.indices
    } {
      if (y(group).exists(_ != 0.0)) {
        val zipped     = y(group).zip(yhat(group))
        val firstClick = zipped.sortBy(-_._2).map(_._1).indexWhere(_ > 0.0)
        mrr += 1.0 / (firstClick + 1)
      }
    }
    mrr / y.length
  }
}
