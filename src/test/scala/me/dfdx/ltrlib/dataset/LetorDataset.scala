package me.dfdx.ltrlib.dataset

import better.files.Resource
import me.dfdx.ltrlib.input.LibsvmInputFormat
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor}
import me.dfdx.ltrlib.model.Feature.SingularFeature

import java.util.zip.GZIPInputStream

object LetorDataset {
  lazy val train = load("/mq2008/train.txt.gz")
  lazy val test  = load("/mq2008/test.txt.gz")

  def load(path: String) = {
    val loader  = LibsvmInputFormat(new GZIPInputStream(Resource.my.getAsStream(path)))
    val spec    = DatasetDescriptor((1 to 46).map(i => SingularFeature(s"f$i")).toList)
    val queries = loader.load(spec)
    Dataset(spec, queries)
  }
}
