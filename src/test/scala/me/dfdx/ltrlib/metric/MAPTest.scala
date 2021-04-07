package me.dfdx.ltrlib.metric

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MAPTest extends AnyFlatSpec with Matchers {
  it should "be 1 on full match" in {
    MAP(2).eval(Array(Array(1.0, 1.0, 0.0)), Array(Array(1.0, 1.0, 0.0))) shouldBe 1.0 +- 0.01
  }

  it should "be 0.5 on half match" in {
    MAP(2).eval(Array(Array(1.0, 0.0, 0.0)), Array(Array(1.0, 1.0, 0.0))) shouldBe 0.5 +- 0.01
  }

  it should "be 0 on no match" in {
    MAP(2).eval(Array(Array(0.0, 0.0, 1.0)), Array(Array(1.0, 1.0, 0.0))) shouldBe 0.0 +- 0.01
  }
}
