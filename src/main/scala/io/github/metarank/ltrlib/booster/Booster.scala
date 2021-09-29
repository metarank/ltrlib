package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Model}
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, RealMatrix, RealVector}

trait Booster[D] extends Model {
  def save(): String
  def trainOneIteration(dataset: D): Unit
  def evalMetric(dataset: D): Double
  def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double]

  override def predict(values: RealMatrix): ArrayRealVector = {
    val rows = values.getRowDimension
    val cols = values.getColumnDimension
    val data = new Array[Double](rows * cols)
    var row  = 0
    while (row < values.getRowDimension) {
      System.arraycopy(values.getRow(row), 0, data, row * cols, cols)
      row += 1
    }
    new ArrayRealVector(predictMat(data, rows, cols))
  }

  override def predict(values: RealVector): Double = {
    predictMat(values.toArray, 1, values.getDimension)(0)
  }

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
