package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import io.github.metarank.ltrlib.booster.{Booster, BoosterDataset}
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.Dataset

case class LambdaMART(
    dataset: Dataset,
    options: BoosterOptions,
    boosterBuilder: (BoosterDataset, BoosterOptions) => Booster
) extends Ranker[Booster] {

  override def fit(): Booster = {
    val x     = new Array[Double](dataset.itemCount * dataset.desc.dim)
    val label = new Array[Double](dataset.itemCount)
    val qid   = new Array[Int](dataset.itemCount)
    var row   = 0
    for {
      group <- dataset.groups
    } {
      cfor(group.labels.indices) { item =>
        {
          label(row) = group.labels(item)
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

    val train = BoosterDataset(dataset, x, label, qid2, dataset.itemCount, dataset.desc.dim)

    val boosterModel = boosterBuilder(train, options)
    cfor(0 until 100) { i =>
      {
        boosterModel.trainOneIteration()
        val err = boosterModel.evalMetric()
        logger.info(s"[$i] err = $err")
      }
    }
    boosterModel
  }

  override def eval(model: Booster, data: Dataset, metric: Metric): Double = {
    val yhat = for {
      group <- data.groups
    } yield {
      model.predictMat(group.values, group.rows, group.columns)
    }
    val y = data.groups.map(_.labels)
    metric.eval(y.toArray, yhat.toArray)
  }
}
