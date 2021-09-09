package io.github.metarank.ltrlib.booster

import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.lightgbm4j.LGBMBooster.FeatureImportanceType

case class LightGBMBooster(model: LGBMBooster) extends Booster[LGBMDataset] {
  override def trainOneIteration(dataset: LGBMDataset): Unit = model.updateOneIter()
  override def evalMetric(dataset: LGBMDataset): Double      = model.getEval(0)(0)

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    model.predictForMat(values, rows, cols, true)
  }

  override def save(): String =
    model.saveModelToString(0, 0, FeatureImportanceType.GAIN)
}

object LightGBMBooster extends BoosterFactory[LGBMDataset, LightGBMBooster] {
  override def formatData(d: BoosterDataset): LGBMDataset = {
    val ds = LGBMDataset.createFromMat(d.data, d.rows, d.cols, true, "")
    ds.setField("label", d.labels.map(_.toFloat))
    ds.setField("group", d.groups)
    ds
  }
  def apply(string: String): LightGBMBooster = {
    LightGBMBooster(LGBMBooster.loadModelFromString(string))
  }
  def apply(ds: LGBMDataset, options: BoosterOptions) = {
    val paramsMap = Map(
      "objective"      -> "lambdarank",
      "metric"         -> "ndcg",
      "num_iterations" -> options.trees.toString,
      "learning_rate"  -> options.learningRate.toString
    )
    val params = paramsMap.map(kv => s"${kv._1}=${kv._2}").mkString(" ")
    new LightGBMBooster(
      model = LGBMBooster.create(ds, params)
    )
  }
}
