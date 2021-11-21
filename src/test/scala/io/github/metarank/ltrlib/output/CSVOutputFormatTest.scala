package io.github.metarank.ltrlib.output

import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import io.github.metarank.ltrlib.model.Feature.{SingularFeature, VectorFeature}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayOutputStream

class CSVOutputFormatTest extends AnyFlatSpec with Matchers {
  it should "export simple CSV" in {
    val desc = DatasetDescriptor(List(SingularFeature("f1"), SingularFeature("f2")))
    val ds   = Dataset(desc, List(Query(desc, List(LabeledItem(1, 1, Array(1.0, 2.0))))))
    val out  = new ByteArrayOutputStream()
    CSVOutputFormat.write(out, ds)
    val str = new String(out.toByteArray)
    str shouldBe """"label","group","f1","f2"
                   |"1.0","1","1.0","2.0"
                   |""".stripMargin
  }

  it should "export CSV with vectors" in {
    val desc = DatasetDescriptor(List(SingularFeature("f1"), VectorFeature("f2", 2)))
    val ds   = Dataset(desc, List(Query(desc, List(LabeledItem(1, 1, Array(1.0, 2.0, 3.0))))))
    val out  = new ByteArrayOutputStream()
    CSVOutputFormat.write(out, ds)
    val str = new String(out.toByteArray)
    str shouldBe """"label","group","f1","f2_0","f2_1"
                   |"1.0","1","1.0","2.0","3.0"
                   |""".stripMargin
  }
}
