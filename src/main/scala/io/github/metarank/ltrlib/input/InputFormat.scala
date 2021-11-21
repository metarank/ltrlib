package io.github.metarank.ltrlib.input

import io.github.metarank.ltrlib.model.{DatasetDescriptor, Query}
import io.github.metarank.ltrlib.util.Logging

import java.io.InputStream

trait InputFormat extends Logging {
  def load(data: InputStream, desc: DatasetDescriptor): List[Query]
}

object InputFormat {
  abstract class DatasetError(msg: String) extends Throwable(msg)
  case class DimensionMismatchError(field: String, index: Int, dim: Int)
      extends DatasetError(s"dimension mismatch on field $field: index=$index size=$dim")
}
