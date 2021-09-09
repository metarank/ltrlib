package io.github.metarank.ltrlib.ranking.pointwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Model}
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.ranking.pointwise.RandomRanker.RandomModel
import scala.util.Random

case class RandomRanker() extends Ranker[RandomModel] {
  override def fit(): RandomModel = RandomModel(Random.nextInt())

}

object RandomRanker {
  case class RandomModel(seed: Int) extends Model {
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
