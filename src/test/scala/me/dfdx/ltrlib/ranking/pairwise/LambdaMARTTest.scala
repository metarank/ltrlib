package me.dfdx.ltrlib.ranking.pairwise

import me.dfdx.ltrlib.booster.LightGBMBooster
import me.dfdx.ltrlib.dataset.LetorDataset
import me.dfdx.ltrlib.metric.MSE
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor" in {
    val lm      = LambdaMART(LetorDataset.train, LightGBMBooster.apply)
    val booster = lm.fit()
    val mse     = lm.eval(booster, LetorDataset.train, MSE)
    val br      = 1
  }
}
