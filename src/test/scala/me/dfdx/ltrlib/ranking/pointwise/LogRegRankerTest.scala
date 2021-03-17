package me.dfdx.ltrlib.ranking.pointwise

import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker.NoOptions
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LogRegRankerTest extends AnyFlatSpec with Matchers {
  it should "train simple model" in {
    val desc = DatasetDescriptor(List(SingularFeature("one"), SingularFeature("two")))
    val dataset = Dataset(
      desc,
      List(
        Query(
          desc,
          List(
            LabeledItem(0, 1, Array(2.7810836, 2.550537003)),
            LabeledItem(0, 1, Array(1.465489372, 2.362125076)),
            LabeledItem(0, 1, Array(3.396561688, 4.400293529)),
            LabeledItem(0, 1, Array(1.38807019, 1.850220317)),
            LabeledItem(0, 1, Array(3.06407232, 3.005305973)),
            LabeledItem(1, 1, Array(7.627531214, 2.759262235)),
            LabeledItem(1, 1, Array(5.332441248, 2.088626775)),
            LabeledItem(1, 1, Array(6.922596716, 1.77106367)),
            LabeledItem(1, 1, Array(8.675418651, -0.242068655)),
            LabeledItem(1, 1, Array(7.673756466, 3.508563011))
          )
        )
      )
    )
    val model = LogRegRanker(dataset).fit(NoOptions())
    model.weights.nonEmpty shouldBe true
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
    val model = LogRegRanker(dataset).fit(NoOptions())
    model.weights.nonEmpty shouldBe true
  }
}
