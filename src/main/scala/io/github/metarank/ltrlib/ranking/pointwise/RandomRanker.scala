package io.github.metarank.ltrlib.ranking.pointwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.Dataset
import io.github.metarank.ltrlib.ranking.Ranker

import scala.util.Random

case class RandomRanker() extends Ranker[Unit] {
  override def fit(): Unit = {}

  override def eval(model: Unit, data: Dataset, metric: Metric): Double = {
    val y = data.groups.map(_.labels)
    val yhat = for {
      group <- data.groups
    } yield {
      val pred = new Array[Double](group.rows)
      cfor(0 until group.rows) { row =>
        pred(row) = Random.nextDouble()
      }
      pred
    }
    metric.eval(y.toArray, yhat.toArray)
  }
}
