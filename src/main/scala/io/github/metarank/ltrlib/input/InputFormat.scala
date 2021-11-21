package io.github.metarank.ltrlib.input

import io.github.metarank.ltrlib.util.Logging

trait InputFormat extends Logging

object InputFormat {
  case class DatasetError(msg: String) extends Throwable(msg)
}
