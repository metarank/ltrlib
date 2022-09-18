package io.github.metarank.ltrlib.model

import io.github.metarank.ltrlib.booster.Booster
import io.github.metarank.ltrlib.model.FitResult.IterationResult

case class FitResult[T <: Model](model: T, iterations: List[IterationResult] = Nil)

object FitResult {
  case class IterationResult(index: Int, trainMetric: Double, testMetric: Double, took: Long)
}
