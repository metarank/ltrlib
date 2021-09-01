package io.github.metarank.ltrlib.util

import org.slf4j.LoggerFactory

trait Logging {
  val logger = LoggerFactory.getLogger(this.getClass)
}
