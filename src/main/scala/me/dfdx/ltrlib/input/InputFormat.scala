package me.dfdx.ltrlib.input

import me.dfdx.ltrlib.input.InputFormat.DatasetError
import me.dfdx.ltrlib.model.{DatasetDescriptor, Query}
import me.dfdx.ltrlib.util.Logging

trait InputFormat extends Logging {
  def load(desc: DatasetDescriptor): List[Query]
}

object InputFormat {
  abstract class DatasetError(msg: String) extends Throwable(msg)
  case class DimensionMismatchError(field: String, index: Int, dim: Int)
      extends DatasetError(s"dimension mismatch on field $field: index=$index size=$dim")
}
