package io.github.metarank.ltrlib.input

import io.github.metarank.ltrlib.model.Feature.SingularFeature
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayInputStream

class CSVInputFormatTest extends AnyFlatSpec with Matchers {
  it should "parse dataset with header" in {
    val data =
      """group,label,f1,f2
        |1,1,0,0
        |1,0,0,0""".stripMargin
    val result = CSVInputFormat.load("group", "label", new ByteArrayInputStream(data.getBytes)).right.get
    result.desc.features shouldBe List(SingularFeature("f1"), SingularFeature("f2"))
    result.queries.size shouldBe 1
  }

  it should "parse quoted vals" in {
    val data =
      """group,label,f1,f2
        |1,1,0,"0"
        |1,0,0,0""".stripMargin
    val result = CSVInputFormat.load("group", "label", new ByteArrayInputStream(data.getBytes))
    result.isRight shouldBe true
  }

}
