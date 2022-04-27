package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.booster.Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.ltrlib.booster.{Booster, BoosterDataset}
import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Feature}
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART.LMartDataset

case class LambdaMART[D, T <: Booster[D], O <: BoosterOptions](
    dataset: Dataset,
    options: O,
    booster: BoosterFactory[D, T, O],
    testDatasetOption: Option[Dataset] = None
) extends Ranker[T] {

  override def fit(): T = {

    val featureNames = dataset.desc.features.flatMap {
      case Feature.SingularFeature(name)     => List(name)
      case Feature.VectorFeature(name, size) => (0 until size).map(i => s"${name}_$i")
    }
    val trainDs = LMartDataset(dataset)
    val train =
      BoosterDataset(
        dataset,
        trainDs.featureValues,
        trainDs.labels,
        trainDs.groups,
        dataset.itemCount,
        dataset.desc.dim,
        featureNames.toArray
      )
    val trainDatasetNative = booster.formatData(train, None)
    val testDatasetNative = for {
      testDataset <- testDatasetOption
      testDs = LMartDataset.apply(testDataset)
    } yield {
      booster.formatData(
        BoosterDataset(
          testDataset,
          testDs.featureValues,
          testDs.labels,
          testDs.groups,
          testDataset.itemCount,
          testDataset.desc.dim,
          featureNames.toArray
        ),
        Some(trainDatasetNative)
      )
    }
    val boosterModel = booster(trainDatasetNative, options)
    cfor(0 until options.trees) { i =>
      {
        boosterModel.trainOneIteration(trainDatasetNative)
        val ndcgTrain = boosterModel.evalMetric(trainDatasetNative)
        testDatasetNative match {
          case Some(value) =>
            val ndcgTest = boosterModel.evalMetric(value)
            logger.info(s"[$i] NDCG@train = $ndcgTrain NDCG@test = $ndcgTest")
          case None => logger.info(s"[$i] NDCG@train = $ndcgTrain")
        }

      }
    }
    boosterModel
  }

}

object LambdaMART {
  case class LMartDataset(featureValues: Array[Double], labels: Array[Double], groups: Array[Int])
  object LMartDataset {
    def apply(dataset: Dataset): LMartDataset = {
      val featureValues = new Array[Double](dataset.itemCount * dataset.desc.dim)
      val label         = new Array[Double](dataset.itemCount)
      val qid           = new Array[Int](dataset.itemCount)
      var row           = 0
      for {
        group <- dataset.groups
      } {
        cfor(group.labels.indices) { item =>
          {
            label(row) = group.labels(item)
            qid(row) = group.group
            cfor(0 until group.columns) { col =>
              featureValues(row * dataset.desc.dim + col) = group.getValue(item, col)
            }
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
      LMartDataset(featureValues, label, qid2)
    }
  }
}
