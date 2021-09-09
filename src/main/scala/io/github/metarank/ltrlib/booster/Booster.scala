package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Model}

trait Booster[D] extends Model {
  def save(): String
  def trainOneIteration(dataset: D): Unit
  def evalMetric(dataset: D): Double
  def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double]

  override def eval(data: Dataset, metric: Metric): Double = {
    val yhat = for {
      group <- data.groups
    } yield {
      predictMat(group.values, group.rows, group.columns)
    }
    val y = data.groups.map(_.labels)
    metric.eval(y.toArray, yhat.toArray)
  }

}

object Booster {
  case class BoosterOptions(trees: Int = 100, learningRate: Double = 0.1)

  trait BoosterFactory[D, T <: Booster[D]] {
    def apply(string: String): T
    def formatData(ds: BoosterDataset): D
    def apply(dataset: D, options: BoosterOptions): T
  }
}
