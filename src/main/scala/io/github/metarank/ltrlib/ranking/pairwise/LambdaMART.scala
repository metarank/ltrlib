package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.booster.Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.ltrlib.booster.{Booster, BoosterDataset}
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Feature}

case class LambdaMART[D, T <: Booster[D]](
    dataset: Dataset,
    options: BoosterOptions,
    booster: BoosterFactory[D, T]
) extends Ranker[T] {

  override def fit(): T = {
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

    val featureNames = dataset.desc.features.flatMap {
      case Feature.SingularFeature(name) => List(name)
      case Feature.VectorFeature(name, size) => (0 until size).map(i => s"${name}_$i")
    }
    val train        = BoosterDataset(dataset, x, label, qid2, dataset.itemCount, dataset.desc.dim, featureNames.toArray)
    val trainDataset = booster.formatData(train)
    val boosterModel = booster(trainDataset, options)
    cfor(0 until options.trees) { i =>
      {
        boosterModel.trainOneIteration(trainDataset)
        val err = boosterModel.evalMetric(trainDataset)
        logger.info(s"[$i] err = $err")
      }
    }
    boosterModel
  }

}
