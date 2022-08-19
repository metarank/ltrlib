package io.github.metarank.ltrlib.ranking.pointwise

import io.github.metarank.ltrlib.model.Feature.SingularFeature
import LogRegRanker.{BatchSGD, NoOptions, SGD}
import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.metric.{MSE, NDCG}
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class LogRegRankerTest extends AnyFlatSpec with Matchers {
  it should "train simple model" in {
    val model = LogRegRanker(LetorDataset.train, BatchSGD(30, 20, 0.3)).fit().model
    model.weights.nonEmpty shouldBe true
    val errTest = model.eval(LetorDataset.test, NDCG(100))
    val errRand = RandomRanker().fit().model.eval(LetorDataset.test, NDCG(100))
    errTest should be > errRand
  }

  it should "train simple model: sgd" in {
    val model = LogRegRanker(LetorDataset.train, SGD(1000, 0.3)).fit().model
    model.weights.nonEmpty shouldBe true
  }

  it should "eval mse" in {
    val logreg = LogRegRanker(LetorDataset.train, BatchSGD(30, 2000, 0.3))
    val model  = logreg.fit().model
    val mse    = model.eval(LetorDataset.train, MSE)
    mse should be > 1.0
  }

  it should "predict one/batch" in {
    val model     = LogRegRanker(LetorDataset.train, SGD(1000, 0.3)).fit().model
    val features  = (1 to 46).map(_ => Random.nextDouble()).toArray
    val resultOne = model.predict(new ArrayRealVector(features))
    val mat       = new Array2DRowRealMatrix(1, 46)
    mat.setRow(0, features)
    val resultBatch = model.predict(mat).getEntry(0)
    resultBatch shouldBe resultOne
  }

  it should "not fail on correlated features" in {
    val desc = DatasetDescriptor(List(SingularFeature("one"), SingularFeature("two")))
    val dataset = Dataset(
      desc,
      List(
        Query(
          desc,
          List(
            LabeledItem(1, 1, Array(1.0, 0.0)),
            LabeledItem(0, 1, Array(0.0, 0.0)),
            LabeledItem(2, 1, Array(1.4, 0.0))
          )
        )
      )
    )
    val model = LogRegRanker(dataset, SGD(10, 0.3)).fit().model
    model.weights.nonEmpty shouldBe true
  }
}
