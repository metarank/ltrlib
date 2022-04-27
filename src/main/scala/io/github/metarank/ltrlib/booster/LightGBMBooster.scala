package io.github.metarank.ltrlib.booster

import io.github.metarank.lightgbm4j.{LGBMBooster, LGBMDataset}
import Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.lightgbm4j.LGBMBooster.FeatureImportanceType

import java.nio.charset.StandardCharsets
import scala.collection.mutable

case class LightGBMBooster(model: LGBMBooster, datasets: mutable.Map[LGBMDataset, Int] = mutable.Map.empty)
    extends Booster[LGBMDataset] {
  override def trainOneIteration(dataset: LGBMDataset): Unit = model.updateOneIter()
  override def evalMetric(dataset: LGBMDataset): Double = {
    datasets.get(dataset) match {
      case Some(index) => model.getEval(index)(0)
      case None =>
        val maxIndex = datasets.values.reduceLeftOption(math.max).getOrElse(0)
        datasets.put(dataset, maxIndex + 1)
        model.addValidData(dataset)
        model.getEval(maxIndex + 1)(0)
    }
  }

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    model.predictForMat(values, rows, cols, true)
  }

  override def save(): Array[Byte] =
    model.saveModelToString(0, 0, FeatureImportanceType.SPLIT).getBytes(StandardCharsets.UTF_8)

  override def weights(): Array[Double] =
    // numIteration=0 means "use all of them"
    // we use split there to match xgboost, which can only do split
    model.featureImportance(0, FeatureImportanceType.SPLIT)
}

object LightGBMBooster extends BoosterFactory[LGBMDataset, LightGBMBooster, LightGBMOptions] {
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
  def apply(ds: LGBMDataset, options: LightGBMOptions) = {
    val paramsMap = Map(
      "objective"                   -> "lambdarank",
      "metric"                      -> "ndcg",
      "lambdarank_truncation_level" -> options.ndcgCutoff.toString,
      "max_depth"                   -> options.maxDepth.toString,
      "learning_rate"               -> options.learningRate.toString,
      "num_leaves"                  -> options.numLeaves.toString,
      "seed"                        -> options.randomSeed.toString
    )
    val params = paramsMap.map(kv => s"${kv._1}=${kv._2}").mkString(" ")
    new LightGBMBooster(
      model = LGBMBooster.create(ds, params)
    )
  }
}
