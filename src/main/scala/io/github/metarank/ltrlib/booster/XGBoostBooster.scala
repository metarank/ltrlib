package io.github.metarank.ltrlib.booster

import Booster.{BoosterFactory, BoosterOptions, DatasetOptions}
import io.github.metarank.lightgbm4j.LGBMDataset
import io.github.metarank.ltrlib.util.Logging
import ml.dmlc.xgboost4j.java.{DMatrix, IObjective, XGBoost}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Base64
import scala.jdk.CollectionConverters._

case class XGBoostBooster(model: ml.dmlc.xgboost4j.java.Booster) extends Booster[DMatrix] with Logging {

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    val mat    = new DMatrix(values.map(_.toFloat), rows, cols, 0.0f)
    val result = model.predict(mat)
    val out    = new Array[Double](rows)
    var i      = 0
    while (i < rows) {
      out(i) = result(i)(0)
      i += 1
    }
    out
  }

  override def close(): Unit = model.dispose()

  override def save(): Array[Byte] = {
    val bytes = new ByteArrayOutputStream()
    model.saveModel(bytes)
    bytes.toByteArray
  }

  override def weights(): Array[Double] = {
    val names   = (0 until model.getNumFeature.toInt).map(i => s"feature$i").toArray
    val weights = model.getFeatureScore(names).asScala
    val result = for {
      name <- names
    } yield {
      weights.get(name).map(_.doubleValue()).getOrElse(0.0)
    }
    result
  }
}

object XGBoostBooster extends BoosterFactory[DMatrix, XGBoostBooster, XGBoostOptions] with Logging {
  override def apply(string: Array[Byte]): XGBoostBooster = {
    val booster = XGBoost.loadModel(new ByteArrayInputStream(string))
    XGBoostBooster(booster)
  }
  override def formatData(d: BoosterDataset, parent: Option[DMatrix]): DMatrix = {
    val mat = new DMatrix(d.data.map(_.toFloat), d.rows, d.cols, 0.0f)
    mat.setLabel(d.labels.map(_.toFloat))
    mat.setGroup(d.groups)
    mat
  }

  override def train(
      dataset: DMatrix,
      test: Option[DMatrix],
      options: XGBoostOptions,
      dso: DatasetOptions
  ): XGBoostBooster = {
    val opts = Map[String, Object](
      "objective"   -> "rank:pairwise",
      "eval_metric" -> s"ndcg@${options.ndcgCutoff}",
      "num_round"   -> Integer.valueOf(options.trees),
      "max_depth"   -> options.maxDepth.toString,
      "eta"         -> options.learningRate.toString,
      "seed"        -> options.randomSeed.toString,
      "subsample"   -> options.subsample.toString
    ).asJava
    val featureTypes = for {
      i <- (0 until dso.dims).toArray
    } yield {
      if (dso.categoryFeatures.contains(i)) "c" else "q"
    }
    dataset.setFeatureTypes(featureTypes)
    test.foreach(_.setFeatureTypes(featureTypes))
    val model: ml.dmlc.xgboost4j.java.Booster = XGBoost.train(dataset, opts, 0, Map.empty.asJava, null, null)

    var it           = 0
    var earlyStop    = false
    var lastBest     = 0.0
    var lastBestIter = 0
    while ((it < options.trees) && !earlyStop) {
      it += 1
      model.update(dataset, 1)
      val ndcgTrain = evalMetric(model, dataset)
      test match {
        case Some(value) =>
          val ndcgTest = evalMetric(model, value)
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
    XGBoostBooster(model)
  }

  override def closeData(d: DMatrix): Unit = d.dispose()

  def evalMetric(model: ml.dmlc.xgboost4j.java.Booster, dataset: DMatrix): Double = {
    val result = model.evalSet(Array(dataset), Array("test"), 1)
    result.split(':').last.toDouble
  }

}
