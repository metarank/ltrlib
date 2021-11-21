package io.github.metarank.ltrlib.ranking.pointwise

import io.github.metarank.ltrlib.model.Feature.{SingularFeature, VectorFeature}
import LogRegRanker.{LogRegModel, NoOptions, RegWeights, RegressionOptions, SingularFeatureWeight, VectorFeatureWeight}
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, RealVector}
import io.github.metarank.cfor._
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Model}
import io.github.metarank.ltrlib.ranking.Ranker

import scala.util.Random

case class LogRegRanker(train: Dataset, options: RegressionOptions) extends Ranker[LogRegModel] {
  val (x, y) = prepare()

  def prepare() = {
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
    (x, y)
  }
  override def fit(): LogRegModel = {
    // fill the data
    val weights = options match {
      case LogRegRanker.SGD(iterations, lr)                 => trainSGD(x, y, iterations, lr)
      case LogRegRanker.BatchSGD(iterations, batchSize, lr) => trainBatchSGD(x, y, iterations, batchSize, lr)
    }

    val featureWeights = for {
      (feature, offset) <- train.desc.offsets.toList.sortBy(_._2)
    } yield {
      feature match {
        case f @ SingularFeature(_)     => SingularFeatureWeight(f, weights.weights.getEntry(offset))
        case f @ VectorFeature(_, size) => VectorFeatureWeight(f, weights.weights.getSubVector(offset, size).toArray)
      }
    }
    LogRegModel(featureWeights, weights.intercept)
  }

  def trainSGD(x: Array2DRowRealMatrix, y: RealVector, iterations: Int, lr: Double) = {
    val weights: RealVector = new ArrayRealVector(train.desc.dim)
    var intercept           = 0.0
    cfor(0 until iterations) { it =>
      {
        var sumError = 0.0
        cfor(0 until x.getRowDimension) { row =>
          {
            val r     = x.getRowVector(row)
            val yhat  = predict(r, weights, intercept)
            val error = y.getEntry(row) - yhat
            sumError += error * error / x.getRowDimension
            // intercept
            intercept += lr * 2 * error / x.getRowDimension
            cfor(0 until x.getColumnDimension) { col =>
              weights.addToEntry(col, lr * 2 * error * r.getEntry(col) / x.getRowDimension)
            }
          }
        }
        logger.debug(s"[$it] error = $sumError")
      }
    }
    RegWeights(intercept, weights)
  }

  def trainBatchSGD(x: Array2DRowRealMatrix, y: RealVector, iterations: Int, batchSize: Int, lr: Double) = {
    var weights: RealVector = new ArrayRealVector(train.desc.dim)
    var intercept           = 0.0
    cfor(0 until iterations) { it =>
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
        weights = weights.add(gradient.mapMultiply(2 * lr))
        intercept += interceptGradient * 2 * lr
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
  case class LogRegModel(weights: List[FeatureWeight], intercept: Double) extends Model {
    val weightsVector = new ArrayRealVector(weights.flatMap {
      case SingularFeatureWeight(_, weight) => List(weight)
      case VectorFeatureWeight(_, weights)  => weights.toList
    }.toArray)

    override def eval(data: Dataset, metric: Metric): Double = {
      val y = data.groups.map(_.labels)
      val yhat = for {
        group <- data.groups
      } yield {
        val pred = new Array[Double](group.rows)
        cfor(0 until group.rows) { row =>
          pred(row) = intercept + group.getRowVector(row).dotProduct(weightsVector)
        }
        pred
      }
      metric.eval(y.toArray, yhat.toArray)
    }

    override def predict(values: RealVector): Double = {
      intercept + values.dotProduct(weightsVector)
    }

  }

  sealed trait RegressionOptions
  case class SGD(iterations: Int, learnRate: Double)                      extends RegressionOptions
  case class BatchSGD(iterations: Int, batchSize: Int, learnRate: Double) extends RegressionOptions
  case class NoOptions()

  case class RegWeights(intercept: Double, weights: RealVector)

}
