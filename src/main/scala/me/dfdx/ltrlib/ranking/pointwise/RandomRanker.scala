package me.dfdx.ltrlib.ranking.pointwise

import io.github.metarank.cfor.cfor
import me.dfdx.ltrlib.metric.Metric
import me.dfdx.ltrlib.model.Dataset
import me.dfdx.ltrlib.ranking.Ranker

import scala.util.Random

case class RandomRanker() extends Ranker[Unit, Unit] {
  override def fit(options: Unit): Unit = {}

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
