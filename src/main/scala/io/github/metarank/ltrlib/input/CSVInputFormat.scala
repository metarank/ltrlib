package io.github.metarank.ltrlib.input

import com.opencsv.CSVReader
import io.github.metarank.ltrlib.input.InputFormat.DatasetError
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import io.github.metarank.ltrlib.model.{DatasetDescriptor, LabeledItem, Query}

import scala.collection.JavaConverters._
import java.io.{InputStream, InputStreamReader}
import scala.util.{Failure, Success, Try}

object CSVInputFormat extends InputFormat {
  case class CSVDataset(desc: DatasetDescriptor, queries: List[Query])

  def load(groupColumn: String, labelColumn: String, data: InputStream): Either[DatasetError, CSVDataset] = {
    val reader       = new CSVReader(new InputStreamReader(data))
    val header       = reader.readNext()
    val headerMap    = header.zipWithIndex.toMap
    val featureNames = header.filter(_ != groupColumn).filter(_ != labelColumn).toList
    val cols         = featureNames.length
    val desc         = DatasetDescriptor(featureNames.map(SingularFeature.apply))
    logger.debug(s"opening CSV file: cols=${header.length}")

    for {
      rows <- parseRows(headerMap, groupColumn, labelColumn, reader, cols)
    } yield {
      val queries = rows
        .groupBy(_.group)
        .map { case (_, rows) =>
          Query(desc, rows)
        }
        .toList
      reader.close()
      logger.debug(s"loaded CSV file: rows=${rows.size} groups=${queries.size}")
      CSVDataset(desc, queries)
    }
  }

  def parseRows(
      header: Map[String, Int],
      groupColumn: String,
      labelColumn: String,
      reader: CSVReader,
      cols: Int
  ): Either[DatasetError, List[LabeledItem]] = for {
    labelCol <- header.get(labelColumn).toRight(DatasetError(s"label column $labelColumn not found in header"))
    groupCol <- header.get(groupColumn).toRight(DatasetError(s"group column $groupColumn not found in header"))
    rows <- Try(reader.iterator().asScala.toList) match {
      case Failure(exception) => Left(DatasetError(s"error parsing: $exception"))
      case Success(value)     => Right(value)
    }
    items <- rows
      .map(row => parseRow(row, groupCol, labelCol, cols))
      .partition(_.isLeft) match {
      case (Nil, values)       => Right(values.flatMap(_.toOption))
      case (Left(err) :: _, _) => Left(err)
    }
  } yield {
    items
  }

  def parseRow(row: Array[String], groupCol: Int, labelCol: Int, dim: Int): Either[DatasetError, LabeledItem] = for {
    label <- Try(row(labelCol).toDouble) match {
      case Failure(exception) => Left(DatasetError(s"cannot parse label for row ${row.toList}: $exception"))
      case Success(value)     => Right(value)
    }
    group <- Try(row(groupCol).toInt) match {
      case Failure(exception) => Left(DatasetError(s"cannot parse group for row ${row.toList}: $exception"))
      case Success(value)     => Right(value)
    }
  } yield {
    val values = new Array[Double](dim)
    var i      = 0
    var j      = 0
    while (i < row.length) {
      if ((i != labelCol) && (i != groupCol)) {
        values(j) = row(i).toDouble
        j += 1
      }
      i += 1
    }
    LabeledItem(label, group, values)
  }

}
