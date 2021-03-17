package me.dfdx.ltrlib

import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{DatasetDescriptor, Query, LabeledItem}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Try}

class QueryTest extends AnyFlatSpec with Matchers {
  val desc = DatasetDescriptor(List(SingularFeature("one")))

  it should "make group from labeled points" in {
    val g = Query(
      desc,
      List(
        LabeledItem(1.0, 1, Array(1)),
        LabeledItem(0.0, 1, Array(0))
      )
    )
    g.labels.toList shouldBe List(1.0, 0.0)
    g.group shouldBe 1
    g.values.toList shouldBe List(1.0, 0.0)
  }

  it should "fail when group ids are different" in {
    val result = Try(
      Query(
        desc,
        List(
          LabeledItem(1.0, 1, Array(1)),
          LabeledItem(0.0, 2, Array(0))
        )
      )
    )
    result shouldBe a[Failure[IllegalArgumentException]]
  }

}
