package io.github.metarank.ltrlib.input

import io.github.metarank.ltrlib.model.{DatasetDescriptor, LabeledItem, Query}

import java.io.{BufferedInputStream, BufferedReader, InputStream, InputStreamReader}
import scala.collection.mutable

object LibsvmInputFormat extends InputFormat {
  override def load(data: InputStream, desc: DatasetDescriptor): List[Query] = {
    val lines           = new BufferedReader(new InputStreamReader(data))
    val groups          = mutable.ArrayBuffer[Query]()
    var lastGroup       = Integer.MIN_VALUE
    val groupBuffer     = mutable.ArrayBuffer[LabeledItem]()
    val firstTick       = System.currentTimeMillis()
    var lastTick        = firstTick
    var rowsRead        = 0
    var rowsReadPerTick = 0
    var memUsed         = 0L
    while (lines.ready()) {
      val line    = lines.readLine()
      val labeled = LibsvmInputFormat.parseLine(desc.dim, line)
      if (labeled.group == lastGroup) {
        // yet another query group member
        groupBuffer.append(labeled)
      } else {
        if (groupBuffer.nonEmpty) {
          // new group found
          val group = Query(desc, groupBuffer.toList)
          groups.append(group)
          memUsed += group.memUsed
          groupBuffer.clear()
        }
        groupBuffer.append(labeled)
        lastGroup = labeled.group
      }
      rowsRead += 1
      rowsReadPerTick += 1
      val now = System.currentTimeMillis()
      if (now - lastTick > 1000) {
        logger.debug(
          s"loaded $rowsRead rows, ${groups.length} groups, ${1000.0 * rowsReadPerTick / (now - lastTick)} rows/sec"
        )
        lastTick = now
        rowsReadPerTick = 0
      }
    }
    // last one
    if (groupBuffer.nonEmpty) {
      val group = Query(desc, groupBuffer.toList)
      groups.append(group)
    }
    logger.debug(
      s"finished loading dataset: $rowsRead rows ${groups.length}, took ${System.currentTimeMillis() - firstTick}ms, ${memUsed} bytes used"
    )
    groups.toList
  }

  val queryPattern = "(qid:)?([0-9]+)".r
  def parseLine(dim: Int, line: String, index: Int = 0): LabeledItem = {
    val tokens = line.split(' ').takeWhile(!_.contains('#'))
    if (tokens.length < 3)
      throw new IllegalArgumentException(
        s"LibSVM format requires at least two columns: label and qid, but got ${tokens.length} on row $index"
      )
    val label = tokens(0).toInt
    val qid = tokens(1) match {
      case queryPattern(_, id) => id.toInt
      case _                   => throw new IllegalArgumentException(s"qid format for item '${tokens(1)}' is not supported on row $index")
    }
    val values = new Array[Double](dim)
    var i      = 2
    while (i < tokens.length) {
      val featureValue = tokens(i).split(':')
      val featureIndex = featureValue(0).toInt
      val value        = featureValue(1).toDouble
      if (value.isNaN || value.isInfinite)
        throw new IllegalArgumentException(
          s"error while parsing line '$line': NaN value on index $featureIndex on row $index"
        )
      if (featureIndex <= 0) {
        throw new IllegalArgumentException("feature index should be strictly greater than zero")
      }
      if (featureIndex > dim) {
        throw new IllegalArgumentException(
          s"feature index is out of range: index=$featureIndex max_index=$dim, row=$index"
        )
      }
      values(featureIndex - 1) = value // libsvm indexing starts from 1
      i += 1
    }
    new LabeledItem(label, qid, values)
  }
}
