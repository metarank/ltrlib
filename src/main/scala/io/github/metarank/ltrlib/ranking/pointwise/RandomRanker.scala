package io.github.metarank.ltrlib.ranking.pointwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, FitResult, Model}
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.ranking.pointwise.RandomRanker.RandomModel
import org.apache.commons.math3.linear.RealVector

import scala.util.Random

case class RandomRanker() extends Ranker[RandomModel, Unit] {
  override def fit(options: Unit): RandomModel = RandomModel(Random.nextInt())

  override def close(): Unit = {}

}

object RandomRanker {
  case class RandomModel(seed: Int) extends Model {
    override def predict(values: RealVector): Double = {
      Random.nextDouble()
    }

    override def eval(data: Dataset, metric: Metric): Double = {
      val rand = new Random(seed)
      val y    = data.groups.map(_.labels)
      val yhat = for {
        group <- data.groups
      } yield {
        val pred = new Array[Double](group.rows)
        cfor(0 until group.rows) { row =>
          pred(row) = rand.nextDouble()
        }
        pred
      }
      metric.eval(y.toArray, yhat.toArray)
    }

  }
}
