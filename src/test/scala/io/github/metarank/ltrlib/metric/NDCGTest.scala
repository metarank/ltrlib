package io.github.metarank.ltrlib.metric

import io.github.metarank.ltrlib.booster.Booster.DatasetOptions
import io.github.metarank.ltrlib.booster.{BoosterDataset, XGBoostBooster, XGBoostOptions}
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, Feature, Query}
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART.FlattenedDataset
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class NDCGTest extends AnyFlatSpec with Matchers {
  it should "compute for wikipedia example" in {
    val y      = Array(Array(3.0, 2.0, 3.0, 0.0, 1.0, 2.0))
    val yhat   = Array(Array(6.0, 5.0, 4.0, 3.0, 2.0, 1.0))
    val result = NDCG(10).eval(y, yhat)
    result shouldBe 0.96 +- 0.01
  }

  it should "be 1.0 on const predictions" in {
    val y    = Array(Array(3.0, 2.0, 1.0))
    val yhat = Array(Array(0.0, 0.0, 0.0))
    NDCG(10).eval(y, yhat) shouldBe 1.0 +- 0.001
  }

  it should "be 0.0 on no labels" in {
    val y    = Array(Array(0.0, 0.0, 0.0))
    val yhat = Array(Array(3.0, 2.0, 1.0))
    NDCG(10).eval(y, yhat) shouldBe 0.0 +- 0.001
  }

  it should "be 1.0 on single item" in {
    val y    = Array(Array(1.0))
    val yhat = Array(Array(1.0))
    NDCG(10).eval(y, yhat) shouldBe 1.0 +- 0.001
  }

  it should "be 1 on perfect ranking" in {
    val y    = Array(Array(3.0, 2.0, 1.0))
    val yhat = Array(Array(3.0, 2.0, 1.0))
    NDCG(10).eval(y, yhat) shouldBe 1.0 +- 0.001
  }

  it should "be 0 on reverse ranking" in {
    val y    = Array(Array(5.0, 4.0, 3.0, 2.0, 1.0))
    val yhat = Array(Array(1.0, 2.0, 3.0, 4.0, 5.0))
    NDCG(10).eval(y, yhat) shouldBe 0.722 +- 0.001
  }

  it should "be low on reverse ranking" in {
    val y    = Array(Array(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
    val yhat = Array(Array(0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
    NDCG(10).eval(y, yhat) shouldBe 0.289 +- 0.001
  }

  it should "be zero on small cutoff on reverse ranking" in {
    val y    = Array(Array(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
    val yhat = Array(Array(0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
    NDCG(3).eval(y, yhat) shouldBe 0.0 +- 0.001
  }

  it should "match spark testsuite for graded relevance" in {
    val y = Array(
      Array(3.0, 0.0, 2.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0),
      Array(0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
      Array(0.0, 0.0, 0.0, 0.0, 0.0)
    )
    val yhat = Array(
      Array(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0),
      Array(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0),
      Array(5.0, 4.0, 3.0, 2.0, 1.0)
    )
    NDCG(3, relpow = true).eval(y, yhat) shouldBe 0.511959 +- 0.001
    NDCG(5, relpow = true).eval(y, yhat) shouldBe 0.487806 +- 0.001
    NDCG(10, relpow = true).eval(y, yhat) shouldBe 0.518700 +- 0.001
    NDCG(15, relpow = true).eval(y, yhat) shouldBe 0.518700 +- 0.001
  }

  it should "match spark suite for binary relevance" in {
    val y = Array(
      Array(1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0),
      Array(0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0),
      Array(0.0, 0.0, 0.0, 0.0, 0.0)
    )
    val yhat = Array(
      Array(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0),
      Array(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0),
      Array(5.0, 4.0, 3.0, 2.0, 1.0)
    )
    NDCG(3, relpow = true).eval(y, yhat) shouldBe 0.333333 +- 0.001
    NDCG(5, relpow = true).eval(y, yhat) shouldBe 0.328788 +- 0.001
    NDCG(10, relpow = true).eval(y, yhat) shouldBe 0.487913 +- 0.001
    NDCG(15, relpow = true).eval(y, yhat) shouldBe 0.487913 +- 0.001
  }

  it should "fuzzer: match xgboost implementation" in {
    val train = (0 until 1000).map(i => makeQuery(i, 2 + Random.nextInt(20), 10)).toList
    val desc  = DatasetDescriptor((0 until 10).map(i => SingularFeature(s"f$i")).toList)
    val opts  = XGBoostOptions(treeMethod = "hist", randomSeed = 0)
    val booster =
      XGBoostBooster.train(
        XGBoostBooster.formatData(dswrap(train, desc), None, XGBoostOptions()),
        None,
        opts,
        DatasetOptions(Array.emptyIntArray, 10)
      )

    for {
      i <- 0 until 100
    } {
      val q1    = makeQuery(1, 1 + Random.nextInt(20), 10)
      val q2    = makeQuery(2, 1 + Random.nextInt(20), 10)
      val q3    = makeQuery(3, 1 + Random.nextInt(20), 10)
      val ds    = dswrap(List(q1, q2, q3), desc)
      val test  = XGBoostBooster.formatData(ds, None, XGBoostOptions())
      val ndcg1 = XGBoostBooster.evalMetric(booster.model, test, i)
      XGBoostBooster.closeData(test)
      val ndcg2 = booster.eval(ds.original, NDCG(10, nolabels = 1.0))
      math.abs(ndcg1 - ndcg2) should be < 0.001
    }
  }

  def makeQuery(group: Int, docs: Int, dim: Int) = Query(
    group = group,
    labels = (0 until docs).map(_ => if (Random.nextBoolean()) 1.0 else 0.0).toArray,
    values = (0 until docs * dim).map(_ => Random.nextDouble()).toArray
  )

  def dswrap(q: List[Query], desc: DatasetDescriptor) = {
    val dataset = Dataset(desc, q)
    val trainDs = FlattenedDataset(dataset)
    val featureNames = dataset.desc.features.flatMap {
      case Feature.SingularFeature(name)     => List(name)
      case Feature.CategoryFeature(name)     => List(name)
      case Feature.VectorFeature(name, size) => (0 until size).map(i => s"${name}_$i")
    }
    BoosterDataset(
      dataset,
      trainDs.featureValues,
      trainDs.labels,
      trainDs.groups,
      trainDs.positions,
      dataset.itemCount,
      dataset.desc.dim,
      featureNames.toArray
    )
  }

}
