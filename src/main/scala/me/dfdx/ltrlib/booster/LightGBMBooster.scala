package me.dfdx.ltrlib.booster

import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import me.dfdx.ltrlib.booster.Booster.BoosterDataset

case class LightGBMBooster(model: LGBMBooster, train: LGBMDataset) extends Booster {
  override def trainOneIteration(): Unit = model.updateOneIter()
  override def evalMetric(): Double      = model.getEval(0)(0)

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    model.predictForMat(values, rows, cols, true)
  }
}

object LightGBMBooster {
  def apply(d: BoosterDataset) = {
    val ds = LGBMDataset.createFromMat(d.data, d.rows, d.cols, true, "")
    ds.setField("label", d.labels.map(_.toFloat))
    ds.setField("group", d.groups)
    new LightGBMBooster(
      model = LGBMBooster.create(ds, s"objective=lambdarank metric=ndcg"),
      train = ds
    )
  }
}
