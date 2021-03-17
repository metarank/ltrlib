package me.dfdx.ltrlib

import better.files.File
import me.dfdx.ltrlib.input.LibsvmInputFormat
import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor}
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker

object Main {
  def main(args: Array[String]): Unit = {
    val desc = DatasetDescriptor((0 until 700).map(i => SingularFeature(s"f$i")).toList)
    val file = File("/home/shutty/work/metarank/yahoo_ltr/set2.train.txt")
    val data = LibsvmInputFormat(file.newInputStream).load(desc)
    val ds   = Dataset(desc, data)
    val lr   = LogRegRanker(ds).fit(null)

    println("hello, world!")
  }
}
