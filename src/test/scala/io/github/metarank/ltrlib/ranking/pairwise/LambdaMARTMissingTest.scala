package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.booster.{LightGBMBooster, LightGBMOptions, XGBoostBooster, XGBoostOptions}
import io.github.metarank.ltrlib.metric.{MSE, NDCG}
import io.github.metarank.ltrlib.model.Dataset
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util
import scala.util.Random

class LambdaMARTMissingTest extends AnyFlatSpec with Matchers {
  lazy val train = zeroify(LetorDataset.train)
  lazy val test  = zeroify(LetorDataset.test)

  it should "train on letor: lightgbm" in {
    val opts    = LightGBMOptions()
    val lm      = LambdaMART(train, LightGBMBooster, Some(test), opts)
    val booster = lm.fit(opts)
    val err     = booster.eval(test, NDCG(10))
    err should be > 0.40
    booster.model.getFeatureNames.length shouldBe 46
  }

  it should "train on letor: xgboost" in {
    val opts = XGBoostOptions()
    val lm      = LambdaMART(train, XGBoostBooster, Some(test), opts)
    val booster = lm.fit(opts)
    val err     = booster.eval(test, NDCG(10))
    err should be > 0.40
  }

  def zeroify(ds: Dataset, fraction: Double = 0.05): Dataset = {
    val queries = ds.groups.map(q => {
      val values = util.Arrays.copyOf(q.values, q.values.length)
      var i      = 0
      while (i < values.length) {
        if (Random.nextDouble() < fraction) values(i) = Double.NaN
        i += 1
      }
      q.copy(values = values)
    })
    ds.copy(groups = queries)
  }

}
