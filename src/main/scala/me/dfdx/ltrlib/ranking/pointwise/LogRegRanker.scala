package me.dfdx.ltrlib.ranking.pointwise

import me.dfdx.ltrlib.model.{Dataset, Feature}
import me.dfdx.ltrlib.model.Feature.{SingularFeature, VectorFeature}
import me.dfdx.ltrlib.ranking.Ranker
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker.{LogRegModel, NoOptions, RegWeights}
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, RealVector}
import io.github.metarank.cfor._

import scala.util.Random

case class LogRegRanker(train: Dataset) extends Ranker[LogRegModel, NoOptions] {
  val LR = 0.3
  val IT = 200
  override def fit(options: NoOptions): LogRegModel = {
    // random weights
    var weights: RealVector = new ArrayRealVector(train.desc.dim)
    var intercept           = 0.0

    // fill the data
    val x   = new Array2DRowRealMatrix(train.itemCount, train.desc.dim)
    val y   = new ArrayRealVector(train.itemCount)
    var row = 0
    for {
      group <- train.groups
    } {
      cfor(group.labels.indices) { item =>
        {
          y.setEntry(row, group.labels(item))
          cfor(0 until group.columns) { col => x.setEntry(row, col, group.getValue(item, col)) }
          row += 1
        }
      }
    }
    //val sgd  = trainSGD(x, y)
    val sgdb = trainBatchSGD(x, y, 100)
    val br   = 1
    ???
  }

  def trainSGD(x: Array2DRowRealMatrix, y: RealVector) = {
    val weights: RealVector = new ArrayRealVector(train.desc.dim)
    var intercept           = 0.0
    cfor(0 until IT) { it =>
      {
        var sumError = 0.0
        cfor(0 until x.getRowDimension) { row =>
          {
            val r     = x.getRowVector(row)
            val yhat  = predict(r, weights, intercept)
            val error = y.getEntry(row) - yhat
            sumError += error * error / x.getRowDimension
            // intercept
            intercept += LR * 2 * error / x.getRowDimension
            cfor(0 until x.getColumnDimension) { col =>
              weights.addToEntry(col, LR * 2 * error * r.getEntry(col) / x.getRowDimension)
            }
          }
        }
        logger.debug(s"[$it] error = $sumError")
      }
    }
    RegWeights(intercept, weights)
  }

  def trainBatchSGD(x: Array2DRowRealMatrix, y: RealVector, batchSize: Int) = {
    var weights: RealVector = new ArrayRealVector(train.desc.dim)
    var intercept           = 0.0
    cfor(0 until IT) { it =>
      {
        var sumError = 0.0
        val sample   = randomSample(0, x.getRowDimension, batchSize)
        // compute gradient for a batch
        val gradient          = new ArrayRealVector(weights.getDimension)
        var interceptGradient = 0.0
        cfor(sample) { sampleId =>
          {
            val r     = x.getRowVector(sampleId)
            val yhat  = predict(r, weights, intercept)
            val error = y.getEntry(sampleId) - yhat
            interceptGradient += error / batchSize
            cfor(0 until weights.getDimension) { w =>
              gradient.addToEntry(w, error * r.getEntry(w) / batchSize)
            }
            sumError += error * error / batchSize
          }
        }
        // set weights
        weights = weights.add(gradient.mapMultiply(2 * LR))
        intercept += interceptGradient * 2 * LR
        logger.debug(s"[$it] error = $sumError")
      }
    }
    RegWeights(intercept, weights)
  }

  def randomSample(from: Int, to: Int, count: Int): Array[Int] = {
    val result    = new Array[Int](count)
    var cur       = 0
    var remaining = to - from
    var cnt       = count
    cfor(from)(x => (x < to) && (cnt > 0), _ + 1) { i =>
      {
        val prob = Random.nextDouble()
        if (prob < (cnt.toDouble / remaining.toDouble)) {
          cnt -= 1
          result(cur) = i
          cur += 1
        }
        remaining -= 1
      }
    }
    result
  }

  def predict(row: RealVector, weights: RealVector, intercept: Double) = {
    val result = intercept + row.dotProduct(weights)
    1.0 / (1.0 + math.exp(-result))
  }
}

object LogRegRanker {
  sealed trait FeatureWeight
  case class SingularFeatureWeight(feature: SingularFeature, weight: Double)     extends FeatureWeight
  case class VectorFeatureWeight(feature: VectorFeature, weights: Array[Double]) extends FeatureWeight
  case class LogRegModel(weights: List[FeatureWeight], residual: Double)

  case class NoOptions()

  case class RegWeights(intercept: Double, weights: RealVector)

}
