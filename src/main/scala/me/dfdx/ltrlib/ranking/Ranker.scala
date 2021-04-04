package me.dfdx.ltrlib.ranking

import me.dfdx.ltrlib.metric.Metric
import me.dfdx.ltrlib.model.Dataset
import me.dfdx.ltrlib.util.Logging

trait Ranker[M, O] extends Logging {
  def fit(options: O): M
  def eval(model: M, data: Dataset, metric: Metric): Double
}
