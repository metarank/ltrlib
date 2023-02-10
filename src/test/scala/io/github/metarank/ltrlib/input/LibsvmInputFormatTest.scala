package io.github.metarank.ltrlib.input

import io.github.metarank.ltrlib.model.LabeledItem
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Try

class LibsvmInputFormatTest extends AnyFlatSpec with Matchers {
  it should "load 1 feature with label and qid" in {
    parse1("1 qid:1 1:1", LabeledItem(1, 1, Array(1.0)))
  }

  it should "load 1 feature with double label and qid" in {
    parse1("1.0 qid:1 1:1", LabeledItem(1, 1, Array(1.0)))
  }

  it should "fail on nan" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qid:1 1:NaN")).isFailure shouldBe true
  }

  it should "fail on negative index" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qid:1 -1:1")).isFailure shouldBe true
  }

  it should "fail on zero index" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qid:1 0:1")).isFailure shouldBe true
  }

  it should "fail on negative qid" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qid:-11 1:NaN")).isFailure shouldBe true
  }

  it should "fail on no features" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qid:1")).isFailure shouldBe true
  }

  it should "fail on wrong qid format" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qidd:1 0:1")).isFailure shouldBe true
  }

  it should "fail on feature index out of range" in {
    Try(LibsvmInputFormat.parseLine(1, "1 qid:1 2:1")).isFailure shouldBe true
  }

  def parse1(line: String, expected: LabeledItem) = {
    val result = LibsvmInputFormat.parseLine(1, line)
    result should matchPattern {
      case LabeledItem(expected.label, expected.group, x) if x(0) == expected.values(0) =>
    }
  }

}
