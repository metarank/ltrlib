package io.github.metarank.ltrlib.dataset

import better.files.Resource
import io.github.metarank.ltrlib.input.LibsvmInputFormat
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor}
import io.github.metarank.ltrlib.model.Feature.SingularFeature

import java.util.zip.GZIPInputStream

object LetorDataset {
  lazy val train = load("/mq2008/train.txt.gz")
  lazy val test  = load("/mq2008/test.txt.gz")

  private def load(path: String) = {
    val loader  = LibsvmInputFormat(new GZIPInputStream(Resource.my.getAsStream(path)))
    val spec    = DatasetDescriptor((1 to 46).map(i => SingularFeature(s"f$i")).toList)
    val queries = loader.load(spec)
    Dataset(spec, queries)
  }
}
