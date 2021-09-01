package io.github.metarank.ltrlib.model

import Feature.{SingularFeature, VectorFeature}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Try}

class DatasetDescriptorTest extends AnyFlatSpec with Matchers {
  it should "compute offsets" in {
    val features = List(
      SingularFeature("one"),
      VectorFeature("two", 10),
      SingularFeature("three")
    )
    val dd = DatasetDescriptor(features)
    dd shouldBe DatasetDescriptor(
      offsets = Map(
        SingularFeature("one")   -> 0,
        VectorFeature("two", 10) -> 1,
        SingularFeature("three") -> 11
      ),
      features = features,
      dim = 12
    )
  }
  it should "fail on duplicate feature names" in {
    val features = List(
      SingularFeature("one"),
      VectorFeature("one", 10)
    )
    Try(DatasetDescriptor(features)) shouldBe a[Failure[_]]
  }
}
