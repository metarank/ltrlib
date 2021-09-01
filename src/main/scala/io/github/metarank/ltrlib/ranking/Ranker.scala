package io.github.metarank.ltrlib.ranking

import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.Dataset
import io.github.metarank.ltrlib.util.Logging

trait Ranker[M] extends Logging {
  def fit(): M
  def eval(model: M, data: Dataset, metric: Metric): Double
}
