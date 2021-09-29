package io.github.metarank.ltrlib.model

import io.github.metarank.ltrlib.metric.Metric
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, RealMatrix, RealVector}

import java.util

trait Model {

  /** Eval a metric over the whole dataset
    * @param data
    * @param metric
    * @return
    */
  def eval(data: Dataset, metric: Metric): Double

  /** Make single prediction
    * @param values
    * @return
    */
  def predict(values: RealVector): Double

  /** Make batch prediction, default impl falling back to per-row predict.
    * You should overload it for better performance.
    * @param values
    * @param rows
    * @param cols
    * @return
    */
  def predict(values: RealMatrix): ArrayRealVector = {
    val rows     = values.getRowDimension
    val results  = new ArrayRealVector(rows)
    var rowIndex = 0
    while (rowIndex < rows) {
      val row = values.getRowVector(rowIndex)
      results.setEntry(rowIndex, predict(row))
      rowIndex += 1
    }
    results
  }
}
