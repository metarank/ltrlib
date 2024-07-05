package io.github.metarank.ltrlib.booster

import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import Booster.{BoosterFactory, BoosterOptions, DatasetOptions}
import com.microsoft.ml.lightgbm.PredictionType
import io.github.metarank.lightgbm4j.LGBMBooster.FeatureImportanceType
import io.github.metarank.ltrlib.util.Logging

import java.nio.charset.StandardCharsets
import scala.collection.mutable

case class LightGBMBooster(model: LGBMBooster) extends Booster[LGBMDataset] with Logging {

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = whenNotClosed {
    model.predictForMat(values, rows, cols, true, PredictionType.C_API_PREDICT_NORMAL)
  }

  override def close(): Unit = whenNotClosed {
    nativeLibIsClosed = true
    model.close()
  }

  override def save(): Array[Byte] = whenNotClosed {
    model.saveModelToString(0, 0, FeatureImportanceType.SPLIT).getBytes(StandardCharsets.UTF_8)
  }

  override def weights(): Array[Double] = whenNotClosed {
    // numIteration=0 means "use all of them"
    // we use split there to match xgboost, which can only do split
    model.featureImportance(0, FeatureImportanceType.SPLIT)
  }
}

object LightGBMBooster extends BoosterFactory[LGBMDataset, LightGBMBooster, LightGBMOptions] with Logging {
  override def formatData(d: BoosterDataset, parent: Option[LGBMDataset], options: LightGBMOptions): LGBMDataset = {
    val ds = LGBMDataset.createFromMat(d.data, d.rows, d.cols, true, "", parent.orNull)
    ds.setField("label", d.labels.map(_.toFloat))
    ds.setField("group", d.groups)
    ds.setFeatureNames(d.featureNames)
    if (options.debias) ds.setField("position", d.positions)
    ds
  }
  def apply(string: Array[Byte]): LightGBMBooster = {
    LightGBMBooster(LGBMBooster.loadModelFromString(new String(string, StandardCharsets.UTF_8)))
  }

  override def closeData(d: LGBMDataset): Unit = d.close()

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
      "feature_fraction"            -> options.featureFraction.toString,
      "eval_at"                     -> options.ndcgCutoff.toString
    )
    val params = paramsMap.map(kv => s"${kv._1}=${kv._2}").mkString(" ")
    val model  = LGBMBooster.create(dataset, params)
    test.foreach(t => model.addValidData(t))
    var it           = 0
    var earlyStop    = false
    var lastBest     = 0.0
    var lastBestIter = 0
    while ((it < options.trees) && !earlyStop) {
      it += 1
      model.updateOneIter()
      val ndcgTrain = model.getEval(0)(0)
      test match {
        case Some(value) =>
          val ndcgTest = model.getEval(1)(0)
          logger.info(s"[$it] NDCG@train = $ndcgTrain NDCG@test = $ndcgTest")
          options.earlyStopping match {
            case Some(esThreshold) =>
              if (ndcgTest > lastBest) {
                lastBest = ndcgTest
                lastBestIter = it
              }
              if ((it - lastBestIter) > esThreshold) {
                logger.info(s"early stop: $esThreshold rounds passed, best=$lastBest last=$ndcgTest")
                earlyStop = true
              }
            case None => //
          }
        case None =>
          logger.info(s"[$it] NDCG@train = $ndcgTrain")
      }
    }
    LightGBMBooster(model)
  }

}
