package io.github.metarank.ltrlib.output

import com.opencsv.{CSVWriter, CSVWriterBuilder}
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, Feature, Query}

import java.io.{OutputStream, OutputStreamWriter}

object CSVOutputFormat extends OutputFormat {
  def write(stream: OutputStream, data: Dataset, header: Boolean) = {
    val writer = new CSVWriter(new OutputStreamWriter(stream))
    if (header) writer.writeNext(writeHeader(data.desc).toArray, false)
    for {
      query <- data.groups
      line  <- writeGroup(query)
    } {
      writer.writeNext(line.toArray, false)
    }
    writer.close()
    logger.debug(s"wrote ${data.groups.size} groups to CSV file")
  }

  def writeGroup(query: Query): List[List[String]] = for {
    rowIndex <- (0 until query.rows).toList
  } yield {
    val row = query.getRow(rowIndex)
    List(query.labels(rowIndex).toString, query.group.toString) ++ row.map(formatNumber)
  }

  def writeHeader(desc: DatasetDescriptor): List[String] = {
    val header = List("label", "group") ++ desc.features.flatMap {
      case Feature.SingularFeature(name)     => List(name)
      case Feature.CategoryFeature(name)     => List(name)
      case Feature.VectorFeature(name, size) => (0 until size).map(i => s"${name}_$i")
    }
    header
  }

  def formatNumber(d: Double): String = {
    if (d % 1 == 0) {
      s"${d.toInt}"
    } else {
      d.toString
    }
  }
}
