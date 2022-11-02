package io.github.metarank.ltrlib.output

import io.github.metarank.ltrlib.model.Dataset

import java.io.OutputStream

object LibSVMOutputFormat extends OutputFormat {
  def write(data: OutputStream, ds: Dataset) = {
    for {
      query <- ds.groups
      rowid <- 0 until query.rows
    } {
      val row  = query.getRow(rowid).zipWithIndex.filter(_._1 != 0).map(x => s"${x._2}:${x._1}")
      val line = s"${query.labels(rowid)} qid:${query.group} ${row.mkString(" ")}\n"
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
        val row  = query.getRow(rowid).zipWithIndex.filter(_._1 != 0).map(x => s"${x._2}:${x._1}")
        val line = s"${query.labels(rowid)} ${row.mkString(" ")}\n"
        data.write(line.getBytes())
      }
    }
  }
}
