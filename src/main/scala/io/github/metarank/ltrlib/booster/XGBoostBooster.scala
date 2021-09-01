package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import Booster.BoosterOptions
import ml.dmlc.xgboost4j.java.{DMatrix, IObjective, XGBoost}

import scala.jdk.CollectionConverters._

case class XGBoostBooster(model: ml.dmlc.xgboost4j.java.Booster, train: DMatrix) extends Booster {

  override def trainOneIteration(): Unit = model.update(train, 1)

  override def evalMetric(): Double = {
    val result = model.evalSet(Array(train), Array("train"), 1)
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
}

object XGBoostBooster {
  def apply(d: BoosterDataset, options: BoosterOptions) = {
    val mat = new DMatrix(d.data.map(_.toFloat), d.rows, d.cols)
    mat.setLabel(d.labels.map(_.toFloat))
    mat.setGroup(d.groups)
    val opts = Map[String, Object](
      "objective"   -> "rank:pairwise",
      "eval_metric" -> "ndcg",
      "num_round"   -> Integer.valueOf(options.trees)
    ).asJava
    new XGBoostBooster(
      model = XGBoost.train(mat, opts, 0, Map.empty.asJava, null, null),
      train = mat
    )
  }
}
