package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.booster.Booster.{BoosterFactory, BoosterOptions}
import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoosterLoadSaveTest extends AnyFlatSpec with Matchers {
  it should "load-save the lightgbm model" in {
    roundtrip(LightGBMBooster, LightGBMOptions())
  }

  it should "load-save the xgboost model" in {
    roundtrip(XGBoostBooster, XGBoostOptions())
  }

  def roundtrip[D, T <: Booster[D], O <: BoosterOptions](booster: BoosterFactory[D, T, O], opts: O) = {
    val lm        = LambdaMART(LetorDataset.train, booster)
    val booster1  = lm.fit(opts)
    val bytes1    = booster1.save()
    val recovered = booster.apply(bytes1)
    val bytes2    = recovered.save()
    bytes1 shouldBe bytes2
  }
}
