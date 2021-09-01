package io.github.metarank.ltrlib.model

import io.github.metarank.cfor._
import org.apache.commons.math3.linear.{ArrayRealVector, RealVector}
case class Query(group: Int, labels: Array[Double], values: Array[Double], columns: Int, rows: Int) {
  val memUsed                      = labels.length * 8 + values.length * 8
  def getValue(row: Int, col: Int) = values(columns * row + col)
  def getRow(row: Int): Array[Double] = {
    val result = new Array[Double](columns)
    cfor(0 until columns) { col => result(col) = values(row * columns + col) }
    result
  }
  def getRowVector(row: Int): ArrayRealVector = new ArrayRealVector(getRow(row))
}

object Query {
  def apply(desc: DatasetDescriptor, values: List[LabeledItem]) = {
    val labels = new Array[Double](values.size)
    val data   = new Array[Double](values.size * desc.dim)
    val group  = values.head.group
    for {
      (item, i) <- values.zipWithIndex
    } {
      if (item.values.length != desc.dim)
        throw new IllegalArgumentException(
          s"group ${item.group} has ${item.values.length} features, but dim is ${desc.dim}"
        )
      if (item.group != group)
        throw new IllegalArgumentException(
          s"All LabeledItems in group should have same query id. expected $group got ${item.group}"
        )
      labels(i) = item.label
      System.arraycopy(item.values, 0, data, desc.dim * i, item.values.length)
    }
    new Query(group, labels, data, desc.dim, values.size)
  }
}
