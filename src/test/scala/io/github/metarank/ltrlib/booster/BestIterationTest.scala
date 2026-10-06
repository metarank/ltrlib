package io.github.metarank.ltrlib.booster

import io.github.metarank.lightgbm4j.LGBMBooster.FeatureImportanceType
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, Query}
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class BestIterationTest extends AnyFlatSpec with Matchers {
  val desc  = DatasetDescriptor(List(SingularFeature("f")))
  val items = 10

  // Train rewards high feature values and test rewards low ones, so test NDCG is best after the first tree
  def dataset(seed: Int, relevantIfHigh: Boolean): Dataset = {
    val random  = new Random(seed)
    val queries = (0 until 100).map { group =>
      val values = Array.fill(items)(random.nextDouble())
      val sorted = values.sorted
      val labels = values.map(v => if (if (relevantIfHigh) v >= sorted(items - 3) else v <= sorted(2)) 1.0 else 0.0)
      Query(group, labels, values)
    }
    Dataset(desc, queries.toList)
  }

  val train = dataset(1, relevantIfHigh = true)
  val test  = dataset(2, relevantIfHigh = false)

  "LightGBM" should "keep only the trees up to the best test iteration" in {
    val opts    = LightGBMOptions(trees = 100, randomSeed = 0, earlyStopping = Some(20))
    val booster = LambdaMART(train, LightGBMBooster, Some(test), opts).fit(opts)
    "(?m)^Tree=".r.findAllIn(booster.model.saveModelToString(0, 0, FeatureImportanceType.SPLIT)).size shouldBe 1
  }

  "XGBoost" should "keep only the trees up to the best test iteration" in {
    val opts    = XGBoostOptions(trees = 100, randomSeed = 0, earlyStopping = Some(20), treeMethod = "exact")
    val booster = LambdaMART(train, XGBoostBooster, Some(test), opts).fit(opts)
    booster.model.getModelDump(null: String, false).length shouldBe 1
  }

  // CatBoost picks its best iteration by its own loss: here iteration 86, before early stopping fires
  "CatBoost" should "keep only the trees up to the best test iteration" in {
    val opts    = CatboostOptions(trees = 100, randomSeed = 0, earlyStopping = Some(20), loggingLevel = "Silent")
    val booster = LambdaMART(train, CatboostBooster, Some(test), opts).fit(opts)
    booster.booster.getTreeCount should be < opts.trees
  }
}
