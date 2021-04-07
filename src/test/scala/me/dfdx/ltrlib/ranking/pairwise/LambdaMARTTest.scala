package me.dfdx.ltrlib.ranking.pairwise

import me.dfdx.ltrlib.booster.Booster.BoosterOptions
import me.dfdx.ltrlib.booster.{LightGBMBooster, XGBoostBooster}
import me.dfdx.ltrlib.dataset.LetorDataset
import me.dfdx.ltrlib.metric.MSE
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor: lightgbm" in {
    val lm      = LambdaMART(LetorDataset.train, BoosterOptions(), LightGBMBooster(_, _))
    val booster = lm.fit()
    val mse     = lm.eval(booster, LetorDataset.train, MSE)
    mse should be > 0.95
  }

  it should "train on letor: xgboost" in {
    val lm      = LambdaMART(LetorDataset.train, BoosterOptions(), XGBoostBooster(_, _))
    val booster = lm.fit()
    val mse     = lm.eval(booster, LetorDataset.train, MSE)
    mse should be > 0.95
  }
}
