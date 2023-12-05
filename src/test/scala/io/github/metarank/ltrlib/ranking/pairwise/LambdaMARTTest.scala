package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.booster.{
  CatboostBooster,
  CatboostOptions,
  LightGBMBooster,
  LightGBMOptions,
  XGBoostBooster,
  XGBoostOptions
}
import io.github.metarank.ltrlib.metric.{MSE, NDCG}
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, Query}
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART.FlattenedDataset
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor: lightgbm" in {
    val opts = LightGBMOptions(earlyStopping = Some(20))
    val lm = LambdaMART(
      LetorDataset.train,
      LightGBMBooster,
      Some(LetorDataset.test),
      opts
    )
    val booster = lm.fit(opts)
    val err     = booster.eval(LetorDataset.test, NDCG(10))
    err should be > 0.40
    booster.model.getFeatureNames.length shouldBe 46
  }

  it should "train on letor: xgboost" in {
    val opts    = XGBoostOptions(earlyStopping = Some(20))
    val lm      = LambdaMART(LetorDataset.train, XGBoostBooster, Some(LetorDataset.test), opts)
    val booster = lm.fit(opts)
    val err     = booster.eval(LetorDataset.test, NDCG(10))
    err should be > 0.40
  }

  it should "train on letor: catboost" in {
    val opts = CatboostOptions(earlyStopping = Some(10))
    val lm = LambdaMART(
      LetorDataset.train,
      CatboostBooster,
      Some(LetorDataset.test),
      opts
    )
    val booster = lm.fit(opts)
    val err     = booster.eval(LetorDataset.test, NDCG(10))
    err should be > 0.40
  }

  it should "get weights: xgboost" in {
    val opts    = XGBoostOptions(trees = 5)
    val lm      = LambdaMART(LetorDataset.train, XGBoostBooster, Some(LetorDataset.test), opts)
    val booster = lm.fit(opts)
    val weights = booster.weights()
    weights.length shouldBe LetorDataset.train.desc.dim
    weights.count(_ > 0) should be > 0
  }

  it should "get weights: lgbm" in {
    val opts    = LightGBMOptions(trees = 5)
    val lm      = LambdaMART(LetorDataset.train, LightGBMBooster, Some(LetorDataset.test), opts)
    val booster = lm.fit(opts)
    val weights = booster.weights()
    weights.length shouldBe LetorDataset.train.desc.dim
    weights.count(_ > 0) should be > 0
  }

  it should "properly flatten the dataset" in {
    val q1   = Query(group = 7, labels = Array(1.0, 0.0), values = Array(1.0, 2.0))
    val q2   = Query(group = 1, labels = Array(1.0, 0.0, 1.0), values = Array(1.0, 2.0, 3.0))
    val q3   = Query(group = 5, labels = Array(1.0), values = Array(1.0))
    val desc = DatasetDescriptor(List(SingularFeature("f1")))
    val ds   = FlattenedDataset(Dataset(desc = desc, groups = List(q1, q2, q3)))
    ds.groups.toList shouldBe List(2, 3, 1)
    ds.labels.toList shouldBe List(1.0, 0.0, 1.0, 0.0, 1.0, 1.0)
    ds.featureValues.toList shouldBe List(1.0, 2.0, 1.0, 2.0, 3.0, 1.0)
  }
}
