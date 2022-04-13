package io.github.metarank.ltrlib.booster

import Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.lightgbm4j.LGBMDataset
import ml.dmlc.xgboost4j.java.{DMatrix, IObjective, XGBoost}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Base64
import scala.jdk.CollectionConverters._

case class XGBoostBooster(model: ml.dmlc.xgboost4j.java.Booster) extends Booster[DMatrix] {

  override def trainOneIteration(dataset: DMatrix): Unit = model.update(dataset, 1)

  override def evalMetric(dataset: DMatrix): Double = {
    val result = model.evalSet(Array(dataset), Array("test"), 1)
    result.split(':').last.toDouble
  }

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    val mat    = new DMatrix(values.map(_.toFloat), rows, cols)
    val result = model.predict(mat)
    val out    = new Array[Double](rows)
    var i      = 0
    while (i < rows) {
      out(i) = result(i)(0)
      i += 1
    }
    out
  }

  override def save(): String = {
    val bytes = new ByteArrayOutputStream()
    model.saveModel(bytes)
    val base64 = Base64.getEncoder.encodeToString(bytes.toByteArray)
    base64
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

object XGBoostBooster extends BoosterFactory[DMatrix, XGBoostBooster] {
  override def apply(string: String): XGBoostBooster = {
    val decoded = Base64.getDecoder.decode(string)
    val booster = XGBoost.loadModel(new ByteArrayInputStream(decoded))
    XGBoostBooster(booster)
  }
  override def formatData(d: BoosterDataset, parent: Option[DMatrix]): DMatrix = {
    val mat = new DMatrix(d.data.map(_.toFloat), d.rows, d.cols)
    mat.setLabel(d.labels.map(_.toFloat))
    mat.setGroup(d.groups)
    mat
  }
  def apply(d: DMatrix, options: BoosterOptions) = {
    val opts = Map[String, Object](
      "objective"   -> "rank:pairwise",
      "eval_metric" -> "ndcg",
      "num_round"   -> Integer.valueOf(options.trees)
    ).asJava
    new XGBoostBooster(
      model = XGBoost.train(d, opts, 0, Map.empty.asJava, null, null)
    )
  }
}
