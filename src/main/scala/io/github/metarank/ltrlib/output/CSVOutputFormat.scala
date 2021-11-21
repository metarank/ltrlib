package io.github.metarank.ltrlib.output

import com.opencsv.CSVWriter
import io.github.metarank.ltrlib.model.{Dataset, Feature}

import java.io.{OutputStream, OutputStreamWriter}

object CSVOutputFormat extends OutputFormat {
  def write(stream: OutputStream, data: Dataset) = {
    val writer = new CSVWriter(new OutputStreamWriter(stream))
    val header = List("label", "group") ++ data.desc.features.flatMap {
      case Feature.SingularFeature(name)     => List(name)
      case Feature.VectorFeature(name, size) => (0 until size).map(i => s"${name}_$i")
    }
    writer.writeNext(header.toArray)
    for {
      group    <- data.groups
      rowIndex <- 0 until group.rows
    } {
      val row  = group.getRow(rowIndex)
      val line = List(group.labels(rowIndex).toString, group.group.toString) ++ row.map(_.toString)
      writer.writeNext(line.toArray)
    }
    writer.close()
    logger.debug(s"wrote ${data.groups.size} groups to CSV file")

  }
}
