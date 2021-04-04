package me.dfdx.ltrlib.ranking.pairwise

import io.github.metarank.cfor.cfor
import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import me.dfdx.ltrlib.metric.Metric
import me.dfdx.ltrlib.model.Dataset
import me.dfdx.ltrlib.ranking.Ranker
import me.dfdx.ltrlib.ranking.pairwise.LambdaMART.BoosterOptions
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector}

case class LambdaMART(dataset: Dataset) extends Ranker[LGBMBooster, BoosterOptions] {

  lazy val ds = prepare

  def prepare = {
    val x     = new Array[Double](dataset.itemCount * dataset.desc.dim)
    val label = new Array[Float](dataset.itemCount)
    val qid   = new Array[Int](dataset.itemCount)
    var row   = 0
    for {
      group <- dataset.groups
    } {
      cfor(group.labels.indices) { item =>
        {
          label(row) = group.labels(item).toFloat
          qid(row) = group.group
          cfor(0 until group.columns) { col => x(row * dataset.desc.dim + col) = group.getValue(item, col) }
          row += 1
        }
      }
    }

    val qid2 = qid
      .groupBy(identity)
      .map { case (q, cnt) =>
        q -> cnt.length
      }
      .toList
      .sortBy(_._1)
      .map(_._2)
      .toArray

    val ds = LGBMDataset.createFromMat(x, dataset.itemCount, dataset.desc.dim, true, "")
    ds.setField("label", label)
    ds.setField("group", qid2)
    ds
  }
  override def fit(options: BoosterOptions): LGBMBooster = {
    val booster = LGBMBooster.create(ds, "objective=lambdarank metric=ndcg")
    cfor(0 until 100) { i =>
      {
        booster.updateOneIter()
        val err = booster.getEval(0)
        logger.info(s"[$i] err = ${err(0)}")
      }
    }
    booster
  }

  override def eval(model: LGBMBooster, data: Dataset, metric: Metric): Double = {
    val yhat = for {
      group <- data.groups
    } yield {
      model.predictForMat(group.values, group.rows, group.columns, true)
    }
    val y = data.groups.map(_.labels)
    metric.eval(y.toArray, yhat.toArray)
  }
}

object LambdaMART {
  sealed trait BoosterOptions
  case class LightGBMBoosterOptions(trees: Int) extends BoosterOptions
}
