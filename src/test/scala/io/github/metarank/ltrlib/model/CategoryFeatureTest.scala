package io.github.metarank.ltrlib.model

import io.github.metarank.ltrlib.booster.{LightGBMBooster, LightGBMOptions, XGBoostBooster, XGBoostOptions}
import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.metric.NDCG
import io.github.metarank.ltrlib.model.Feature.{CategoryFeature, SingularFeature}
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class CategoryFeatureTest extends AnyFlatSpec with Matchers {
  val desc = DatasetDescriptor(List(SingularFeature("one"), CategoryFeature("two")))
  def generateQuery(desc: DatasetDescriptor, size: Int, id: Int) = Query(
    desc,
    for {
      _ <- (0 until size).toList
    } yield {
      LabeledItem(Random.nextInt(2), id, Array(Random.nextDouble(), Random.nextInt(4).toDouble))
    }
  )

  val dataset = Dataset(
    desc = desc,
    groups = (0 until 1000).map(id => generateQuery(desc, 10, id)).toList
  )

  it should "properly pass cat feature in lightgbm" in {
    val opts       = LightGBMOptions(trees = 10)
    val lm         = LambdaMART(dataset, LightGBMBooster, None, opts)
    val booster    = lm.fit(opts)
    val importance = booster.model.getFeatureNames.zip(booster.weights())
    importance.length shouldBe 2
  }

  it should "properly pass cat feature in xgboost" in {
    val opts       = XGBoostOptions(trees = 10)
    val lm         = LambdaMART(dataset, XGBoostBooster, None, opts)
    val booster    = lm.fit(opts)
    val importance = booster.weights()
    importance.length shouldBe 2
  }
}
