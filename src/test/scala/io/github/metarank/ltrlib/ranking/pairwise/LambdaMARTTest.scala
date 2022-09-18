package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import io.github.metarank.ltrlib.booster.{LightGBMBooster, LightGBMOptions, XGBoostBooster, XGBoostOptions}
import io.github.metarank.ltrlib.metric.{MSE, NDCG}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor: lightgbm" in {
    val lm      = LambdaMART(LetorDataset.train, LightGBMOptions(), LightGBMBooster, Some(LetorDataset.test))
    val booster = lm.fit()
    val err     = booster.model.eval(LetorDataset.test, NDCG(10))
    err should be > 0.40
    booster.model.model.getFeatureNames.length shouldBe 46
  }

  it should "train on letor: xgboost" in {
    val lm      = LambdaMART(LetorDataset.train, XGBoostOptions(), XGBoostBooster, Some(LetorDataset.test))
    val booster = lm.fit()
    val err     = booster.model.eval(LetorDataset.test, NDCG(10))
    err should be > 0.40
  }

  it should "get weights: xgboost" in {
    val lm      = LambdaMART(LetorDataset.train, XGBoostOptions(trees = 5), XGBoostBooster, Some(LetorDataset.test))
    val booster = lm.fit()
    val weights = booster.model.weights()
    weights.length shouldBe LetorDataset.train.desc.dim
    weights.count(_ > 0) should be > 0
  }

  it should "get weights: lgbm" in {
    val lm      = LambdaMART(LetorDataset.train, LightGBMOptions(trees = 5), LightGBMBooster, Some(LetorDataset.test))
    val booster = lm.fit()
    val weights = booster.model.weights()
    weights.length shouldBe LetorDataset.train.desc.dim
    weights.count(_ > 0) should be > 0
  }
}
