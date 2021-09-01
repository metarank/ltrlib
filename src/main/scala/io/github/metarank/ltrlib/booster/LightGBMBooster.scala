package io.github.metarank.ltrlib.booster

import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import Booster.BoosterOptions

case class LightGBMBooster(model: LGBMBooster, train: LGBMDataset) extends Booster {
  override def trainOneIteration(): Unit = model.updateOneIter()
  override def evalMetric(): Double      = model.getEval(0)(0)

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    model.predictForMat(values, rows, cols, true)
  }
}

object LightGBMBooster {
  def apply(d: BoosterDataset, options: BoosterOptions) = {
    val ds = LGBMDataset.createFromMat(d.data, d.rows, d.cols, true, "")
    ds.setField("label", d.labels.map(_.toFloat))
    ds.setField("group", d.groups)
    val params = Map(
      "objective"      -> "lambdarank",
      "metric"         -> "ndcg",
      "num_iterations" -> options.trees.toString,
      "learning_rate"  -> options.learningRate.toString
    )
    new LightGBMBooster(
      model = LGBMBooster.create(ds, params.map(kv => s"${kv._1}=${kv._2}").mkString(" ")),
      train = ds
    )
  }
}
