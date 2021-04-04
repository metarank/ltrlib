package me.dfdx.ltrlib.metric

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NDCGTest extends AnyFlatSpec with Matchers {
  it should "compute for wikipedia example" in {
    val y      = Array(Array(3.0, 2.0, 3.0, 0.0, 1.0, 2.0))
    val yhat   = Array(Array(3.0, 3.0, 2.0, 2.0, 1.0, 0.0))
    val result = NDCG(10).eval(y, yhat)
    result shouldBe 0.94 +- 0.01
  }
}
