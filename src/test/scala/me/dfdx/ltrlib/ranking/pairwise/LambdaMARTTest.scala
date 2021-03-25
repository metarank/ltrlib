package me.dfdx.ltrlib.ranking.pairwise

import me.dfdx.ltrlib.dataset.LetorDataset
import me.dfdx.ltrlib.metric.MSE
import me.dfdx.ltrlib.ranking.pairwise.LambdaMART.LightGBMBoosterOptions
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor" in {
    val lm      = LambdaMART(LetorDataset.train)
    val booster = lm.fit(LightGBMBoosterOptions(10))
    val mse     = lm.eval(booster, MSE)
    val br      = 1
  }
}
