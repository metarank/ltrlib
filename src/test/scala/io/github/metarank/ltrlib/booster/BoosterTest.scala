package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.booster.Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoosterTest extends AnyFlatSpec with Matchers {
  it should "load-save the lightgbm model" in {
    roundtrip(LightGBMBooster)
  }

  it should "load-save the xgboost model" in {
    roundtrip(XGBoostBooster)
  }

  def roundtrip[D, T <: Booster[D]](booster: BoosterFactory[D, T]) = {
    val lm        = LambdaMART(LetorDataset.train, BoosterOptions(trees = 10), booster)
    val booster1  = lm.fit()
    val bytes1    = booster1.save()
    val recovered = booster.apply(bytes1)
    val bytes2    = recovered.save()
    bytes1 shouldBe bytes2
  }
}
