package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import io.github.metarank.ltrlib.booster.{LightGBMBooster, XGBoostBooster}
import io.github.metarank.ltrlib.metric.{MSE, NDCG}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor: lightgbm" in {
    val lm      = LambdaMART(LetorDataset.train, BoosterOptions(), LightGBMBooster)
    val booster = lm.fit()
    val err     = booster.eval(LetorDataset.train, NDCG(100))
    err should be > 0.70
    booster.model.getFeatureNames.length shouldBe 46
  }

  it should "train on letor: xgboost" in {
    val lm      = LambdaMART(LetorDataset.train, BoosterOptions(), XGBoostBooster)
    val booster = lm.fit()
    val err     = booster.eval(LetorDataset.test, NDCG(100))
    err should be > 0.45
  }
}
