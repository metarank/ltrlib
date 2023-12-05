package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.cfor.cfor
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.booster.Booster.{BoosterFactory, BoosterOptions, DatasetOptions}
import io.github.metarank.ltrlib.booster.{Booster, BoosterDataset}
import io.github.metarank.ltrlib.model.{Dataset, Feature, FitResult}

case class LambdaMART[D, T <: Booster[D], O <: BoosterOptions](
    booster: BoosterFactory[D, T, O],
    train: D,
    test: Option[D],
    categorical: Array[Int],
    dim: Int
) extends Ranker[T, O] {

  override def fit(options: O): T = {
    booster.train(train, test, options, DatasetOptions(categorical, dim))
  }

  override def close(): Unit = {
    booster.closeData(train)
    test.foreach(booster.closeData)
  }

}

object LambdaMART {
  def apply[D, T <: Booster[D], O <: BoosterOptions](
      dataset: Dataset,
      booster: BoosterFactory[D, T, O],
      testDatasetOption: Option[Dataset] = None,
      options: O
  ): LambdaMART[D, T, O] = {
    val featureNames = dataset.desc.features.flatMap {
      case Feature.SingularFeature(name)     => List(name)
      case Feature.CategoryFeature(name)     => List(name)
      case Feature.VectorFeature(name, size) => (0 until size).map(i => s"${name}_$i")
    }
    val trainDs = FlattenedDataset(dataset)
    val train =
      BoosterDataset(
        dataset,
        trainDs.featureValues,
        trainDs.labels,
        trainDs.groups,
        trainDs.positions,
        dataset.itemCount,
        dataset.desc.dim,
        featureNames.toArray
      )
    val trainDatasetNative = booster.formatData(train, None, options)
    val testDatasetNative = for {
      testDataset <- testDatasetOption
      testDs = FlattenedDataset.apply(testDataset)
    } yield {
      booster.formatData(
        BoosterDataset(
          testDataset,
          testDs.featureValues,
          testDs.labels,
          testDs.groups,
          testDs.positions,
          testDataset.itemCount,
          testDataset.desc.dim,
          featureNames.toArray
        ),
        Some(trainDatasetNative),
        options
      )
    }
    new LambdaMART(booster, trainDatasetNative, testDatasetNative, train.categoricalIndices, dataset.desc.dim)
  }

  case class FlattenedDataset(
      featureValues: Array[Double],
      labels: Array[Double],
      groups: Array[Int],
      positions: Array[Int]
  )
  object FlattenedDataset {
    def apply(dataset: Dataset): FlattenedDataset = {
      val featureValues = new Array[Double](dataset.itemCount * dataset.desc.dim)
      val labels        = new Array[Double](dataset.itemCount)
      val qids          = new Array[Int](dataset.groups.size)
      val positions     = new Array[Int](dataset.itemCount)

      var row = 0
      var qid = 0
      var ind = 0
      for {
        group <- dataset.groups
      } {
        qids(qid) = group.labels.length

        var i = 0
        while (i < group.rows) {
          labels(row) = group.labels(i)
          positions(row) = i
          row += 1
          i += 1
        }
        var j = 0
        while (j < group.values.length) {
          featureValues(ind) = group.values(j)
          ind += 1
          j += 1
        }
        qid += 1
      }
      FlattenedDataset(featureValues, labels, qids, positions)
    }
  }
}
