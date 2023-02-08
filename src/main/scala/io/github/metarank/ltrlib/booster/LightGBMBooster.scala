package io.github.metarank.ltrlib.booster

import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import Booster.{BoosterFactory, BoosterOptions, DatasetOptions}
import com.microsoft.ml.lightgbm.PredictionType
import io.github.metarank.lightgbm4j.LGBMBooster.FeatureImportanceType
import io.github.metarank.ltrlib.util.Logging

import java.nio.charset.StandardCharsets
import scala.collection.mutable

case class LightGBMBooster(model: LGBMBooster) extends Booster[LGBMDataset] with Logging {

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    model.predictForMat(values, rows, cols, true, PredictionType.C_API_PREDICT_NORMAL)
  }

  override def save(): Array[Byte] =
    model.saveModelToString(0, 0, FeatureImportanceType.SPLIT).getBytes(StandardCharsets.UTF_8)

  override def weights(): Array[Double] =
    // numIteration=0 means "use all of them"
    // we use split there to match xgboost, which can only do split
    model.featureImportance(0, FeatureImportanceType.SPLIT)
}

object LightGBMBooster extends BoosterFactory[LGBMDataset, LightGBMBooster, LightGBMOptions] with Logging {
  override def formatData(d: BoosterDataset, parent: Option[LGBMDataset]): LGBMDataset = {
    val ds = LGBMDataset.createFromMat(d.data, d.rows, d.cols, true, "", parent.orNull)
    ds.setField("label", d.labels.map(_.toFloat))
    ds.setField("group", d.groups)
    ds.setFeatureNames(d.featureNames)
    ds
  }
  def apply(string: Array[Byte]): LightGBMBooster = {
    LightGBMBooster(LGBMBooster.loadModelFromString(new String(string, StandardCharsets.UTF_8)))
  }

  override def train(
      dataset: LGBMDataset,
      test: Option[LGBMDataset],
      options: LightGBMOptions,
      dso: DatasetOptions
  ): LightGBMBooster = {
    val paramsMap = Map(
      "objective"                   -> "lambdarank",
      "metric"                      -> "ndcg",
      "lambdarank_truncation_level" -> options.ndcgCutoff.toString,
      "max_depth"                   -> options.maxDepth.toString,
      "learning_rate"               -> options.learningRate.toString,
      "num_leaves"                  -> options.numLeaves.toString,
      "seed"                        -> options.randomSeed.toString,
      "categorical_feature"         -> dso.categoryFeatures.mkString(","),
      "feature_fraction"            -> options.featureFraction.toString
    )
    val params = paramsMap.map(kv => s"${kv._1}=${kv._2}").mkString(" ")
    val model  = LGBMBooster.create(dataset, params)
    test.foreach(t => model.addValidData(t))
    for {
      it <- 0 until options.trees
    } {
      model.updateOneIter()
      val ndcgTrain = model.getEval(0)(0)
      val ndcgTest = test match {
        case Some(value) =>
          val ndcgTest = model.getEval(1)(0)
          logger.info(s"[$it] NDCG@train = $ndcgTrain NDCG@test = $ndcgTest")
          ndcgTest
        case None =>
          logger.info(s"[$it] NDCG@train = $ndcgTrain")
          0.0
      }
    }
    LightGBMBooster(model)
  }

}
