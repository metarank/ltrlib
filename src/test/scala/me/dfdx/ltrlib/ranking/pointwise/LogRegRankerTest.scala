package me.dfdx.ltrlib.ranking.pointwise

import me.dfdx.ltrlib.dataset.{LinearDataset, SmallDiabetesDataset}
import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker.NoOptions
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LogRegRankerTest extends AnyFlatSpec with Matchers {
  it should "train simple model" in {
    val model = LogRegRanker(SmallDiabetesDataset()).fit(NoOptions())
    model.weights.nonEmpty shouldBe true
  }

//  it should "not fail on correlated features" in {
//    val desc = DatasetDescriptor(List(SingularFeature("one"), SingularFeature("two")))
//    val dataset = Dataset(
//      desc,
//      List(
//        Query(
//          desc,
//          List(
//            LabeledItem(1, 1, Array(1.0, 0.0)),
//            LabeledItem(0, 1, Array(0.0, 0.0)),
//            LabeledItem(2, 1, Array(1.4, 0.0))
//          )
//        )
//      )
//    )
//    val model = LogRegRanker(dataset).fit(NoOptions())
//    model.weights.nonEmpty shouldBe true
//  }
}