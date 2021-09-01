package io.github.metarank.ltrlib.metric

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MRRTest extends AnyFlatSpec with Matchers {
  it should "compute wikipedia example" in {
    val result = MRR.eval(Array(Array(0.0, 0.0, 1.0)), Array(Array(3.0, 2.0, 1.0)))
    result shouldBe 0.33 +- 0.01
  }

  it should "not fail with click on first position" in {
    val result = MRR.eval(Array(Array(1.0, 1.0, 1.0)), Array(Array(3.0, 2.0, 1.0)))
    result shouldBe 1.0 +- 0.01
  }
}
