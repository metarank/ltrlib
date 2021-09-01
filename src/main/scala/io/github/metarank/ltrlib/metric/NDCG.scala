package io.github.metarank.ltrlib.metric

import io.github.metarank.cfor._

case class NDCG(cutoff: Int) extends Metric {
  override def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double = {
    var ndcg = 0.0
    cfor(y.indices) { group =>
      {
        var dcg = 0.0
        if (y(group).exists(_ != 0.0)) {
          val zipped             = y(group).zip(yhat(group))
          val sortedByPrediction = zipped.sortBy(-_._2).map(_._1)
          cfor(0 until math.min(y(group).length, cutoff)) { doc =>
            dcg += (math.pow(2.0, sortedByPrediction(doc)) - 1.0) / (math.log10(doc + 2) / math.log10(2))
          }
          var idcg         = 0.0
          val sortedByReal = y(group).sorted.reverse
          cfor(0 until math.min(y(group).length, cutoff)) { doc =>
            idcg += (math.pow(2.0, sortedByReal(doc)) - 1.0) / (math.log10(doc + 2) / math.log10(2))
          }
          ndcg += dcg / idcg
        }
      }
    }
    ndcg / y.length
  }
}
