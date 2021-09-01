package io.github.metarank.ltrlib.ranking.pointwise

import io.github.metarank.ltrlib.model.Feature.SingularFeature
import LogRegRanker.{BatchSGD, NoOptions, SGD}
import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.metric.MSE
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LogRegRankerTest extends AnyFlatSpec with Matchers {
  it should "train simple model" in {
    val model = LogRegRanker(LetorDataset.train, BatchSGD(30, 2000)).fit()
    model.weights.nonEmpty shouldBe true
  }

  it should "eval mse" in {
    val logreg = LogRegRanker(LetorDataset.train, BatchSGD(30, 2000))
    val model  = logreg.fit()
    val mse    = logreg.eval(model, LetorDataset.train, MSE)
    mse should be > 1.0
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
    val model = LogRegRanker(dataset, SGD(10)).fit()
    model.weights.nonEmpty shouldBe true
  }
}
