package io.github.metarank.ltrlib.model

import io.github.metarank.ltrlib.metric.Metric

trait Model {
  def eval(data: Dataset, metric: Metric): Double
}
