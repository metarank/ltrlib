package io.github.metarank.ltrlib.output

import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayOutputStream
import scala.collection.immutable.List

class LibSVMOutputFormatTest extends AnyFlatSpec with Matchers {
  val desc = DatasetDescriptor(List(SingularFeature("f1"), SingularFeature("f2")))
  val ds = Dataset(
    desc,
    List(
      Query(desc, List(LabeledItem(1, 1, Array(1.0, 2.0)), LabeledItem(0, 1, Array(0.0, 0.0)))),
      Query(desc, List(LabeledItem(1, 2, Array(1.0, Double.NaN))))
    )
  )

  it should "export with qid label" in {
    val out = new ByteArrayOutputStream()
    LibSVMOutputFormat.write(out, ds)
    val str = new String(out.toByteArray)
    str shouldBe
      """1.0 qid:1 0:1.0 1:2.0
        |0.0 qid:1 
        |1.0 qid:2 0:1.0 1:NaN
        |""".stripMargin
  }

  it should "export with separate groups file" in {
    val data   = new ByteArrayOutputStream()
    val groups = new ByteArrayOutputStream()
    LibSVMOutputFormat.write(data, groups, ds)
    val str  = new String(data.toByteArray)
    val gstr = new String(groups.toByteArray)
    str shouldBe
      """1.0 0:1.0 1:2.0
        |0.0 
        |1.0 0:1.0 1:NaN
        |""".stripMargin
    gstr shouldBe "2\n1\n"
  }
}
