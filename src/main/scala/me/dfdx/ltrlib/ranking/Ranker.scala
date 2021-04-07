package me.dfdx.ltrlib.ranking

import me.dfdx.ltrlib.metric.Metric
import me.dfdx.ltrlib.model.Dataset
import me.dfdx.ltrlib.util.Logging

trait Ranker[M] extends Logging {
  def fit(): M
  def eval(model: M, data: Dataset, metric: Metric): Double
}
