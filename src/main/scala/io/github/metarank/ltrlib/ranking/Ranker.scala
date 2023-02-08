package io.github.metarank.ltrlib.ranking

import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, FitResult, Model}
import io.github.metarank.ltrlib.util.Logging

trait Ranker[M <: Model] extends Logging {
  def fit(): M

}
