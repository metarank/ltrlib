package me.dfdx.ltrlib.math

import java.util

case class Matrix(rows: Int, cols: Int, data: Array[Double]) {
  def set(row: Int, col: Int, value: Double) =
    data(row * cols + col) = value
  def get(row: Int, col: Int) = data(row * cols + col)
  def row(row: Int)           = util.Arrays.copyOfRange(data, row * cols, (row + 1) * cols)
}

object Matrix {
  def apply(rows: Int, cols: Int) = new Matrix(rows, cols, new Array[Double](rows * cols))
}
