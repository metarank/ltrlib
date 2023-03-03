package io.github.metarank.ltrlib.metric

import it.unimi.dsi.fastutil.{Arrays, Swapper}
import it.unimi.dsi.fastutil.ints.IntComparator

case class NDCG(cutoff: Int, relpow: Boolean = false, nolabels: Double = 0.0, single: Double = 1.0) extends Metric {
  val base: Double = Math.exp(1.0) // not important

  override def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double = {
    var ndcg = 0.0
    var i    = 0
    while (i < y.length) {
      ndcg += evalOne(y(i), yhat(i), cutoff)
      i += 1
    }
    ndcg / y.length
  }

  def evalOne(y: Array[Double], yhat: Array[Double], k: Int): Double = {
    if (isZero(y)) {
      // all labels are zero
      nolabels
    } else if (y.length == 1) {
      single
    } else {
      var dcg                = 0.0
      var idcg               = 0.0
      var i                  = 0
      val sortedByPrediction = NDCG.sort2(y, yhat)
      val sortedByLabel      = NDCG.sort2(y, y)
      while (i < math.min(y.length, k)) {
        if (relpow) {
          dcg += (math.pow(2.0, sortedByPrediction(i)) - 1) / log(base, i + 2)
          idcg += (math.pow(2.0, sortedByLabel(i)) - 1) / log(base, i + 2)
        } else {
          dcg += sortedByPrediction(i) / log(base, i + 2)
          idcg += sortedByLabel(i) / log(base, i + 2)
        }
        i += 1
      }
      if (idcg == 0.0) 0 else dcg / idcg
    }
  }

  def log(base: Double, value: Double) = math.log(value) / math.log(base)

  def isZero(values: Array[Double]): Boolean = {
    var zero = true
    var i    = 0
    while (zero && (i < values.length)) {
      zero = (values(i) == 0.0)
      i += 1
    }
    zero
  }
}

object NDCG {
  def sort2(target: Array[Double], by: Array[Double]): Array[Double] = {
    val tc  = new Array[Double](target.length)
    val byc = new Array[Double](target.length)
    var i   = 0
    while (i < tc.length) {
      tc(i) = target(i)
      byc(i) = by(i)
      i += 1
    }

    val cmp = new IntComparator {
      override def compare(k1: Int, k2: Int): Int = -java.lang.Double.compare(byc(k1), byc(k2))
    }
    val swap = new Swapper {
      override def swap(a: Int, b: Int): Unit = {
        val t1 = tc(a)
        tc(a) = tc(b)
        tc(b) = t1
        val t2 = byc(a)
        byc(a) = byc(b)
        byc(b) = t2
      }
    }
    Arrays.quickSort(0, tc.length, cmp, swap)
    tc
  }

}
