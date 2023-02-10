package io.github.metarank.ltrlib.output

import io.github.metarank.ltrlib.model.Dataset

import java.io.OutputStream

object LibSVMOutputFormat extends OutputFormat {
  def write(data: OutputStream, ds: Dataset, offset: Int = 0) = {
    for {
      query <- ds.groups
      rowid <- 0 until query.rows
    } {
      val features = query.getRow(rowid).zipWithIndex.filter(_._1 != 0).map(x => s"${x._2 + offset}:${x._1}")
      val label    = math.round(query.labels(rowid)).toString
      val line     = (List(label, s"qid:${query.group}") ++ features).mkString("", " ", "\n")
      data.write(line.getBytes())
    }
  }

  def write(data: OutputStream, groups: OutputStream, ds: Dataset) = {
    for {
      query <- ds.groups
    } {
      groups.write(s"${query.rows}\n".getBytes())
      for {
        rowid <- 0 until query.rows
      } {
        val features = query.getRow(rowid).zipWithIndex.filter(_._1 != 0).map(x => s"${x._2}:${x._1}").toList
        val label    = math.round(query.labels(rowid)).toString
        val line     = (List(label) ++ features).mkString("", " ", "\n")
        data.write(line.getBytes())
      }
    }
  }
}
