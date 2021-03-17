package me.dfdx.ltrlib.ranking.pointwise

import me.dfdx.ltrlib.math.Matrix
import me.dfdx.ltrlib.model.{Dataset, Feature}
import me.dfdx.ltrlib.model.Feature.{SingularFeature, VectorFeature}
import me.dfdx.ltrlib.ranking.Ranker
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker.{LogRegModel, NoOptions}
import spire.implicits._

import scala.util.Random

case class LogRegRanker(train: Dataset) extends Ranker[LogRegModel, NoOptions] {
  val LR = 0.3
  val IT = 20
  override def fit(options: NoOptions): LogRegModel = {
    // random weights
    val weights = new Array[Double](train.desc.dim + 1) // intercept
    //cforRange(0 until train.desc.dim) { i => weights(i) = math.abs(Random.nextGaussian()) }

    // fill the data
    val x   = Matrix(train.itemCount, train.desc.dim)
    val y   = new Array[Double](train.itemCount)
    var row = 0
    for {
      group <- train.groups
    } {
      cforRange(group.labels.indices) { item =>
        {
          y(row) = group.labels(item)
          cforRange(0 until group.columns) { col => x.set(row, col, group.getValue(item, col)) }
          row += 1
        }
      }
    }
    cforRange(0 until IT) { it =>
      {
        var sumError = 0.0
        //val sample   = randomSample(0, x.rows, 100)
        cforRange(0 until x.rows) { row =>
          {
            val r     = x.row(row)
            val yhat  = predict(r, weights)
            val error = y(row) - yhat
            sumError += error * error
            // intercept
            weights(0) += LR * error * yhat * (1 - yhat)
            cforRange(0 until x.cols) { col =>
              {
                weights(col + 1) += LR * error * yhat * (1 - yhat) * r(col)
              }
            }
          }
        //val meanError =
        }
        println(s"[$it] error = $sumError")

      }
    }
    val br = 1
    ???
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

  def predict(row: Array[Double], weights: Array[Double]) = {
    var result = weights(0)
    cfor(0)(_ < row.length, _ + 1) { i => result += row(i) * weights(i + 1) }
    1.0 / (1.0 + math.exp(-result))
  }
  def sigmoid(x: Double) = 1.0 / (1 + math.exp(-x))
}

object LogRegRanker {
  sealed trait FeatureWeight
  case class SingularFeatureWeight(feature: SingularFeature, weight: Double)     extends FeatureWeight
  case class VectorFeatureWeight(feature: VectorFeature, weights: Array[Double]) extends FeatureWeight
  case class LogRegModel(weights: List[FeatureWeight], residual: Double)

  case class NoOptions()

}
