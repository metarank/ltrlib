package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Model}
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, RealMatrix, RealVector}

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

trait Booster[D] extends Model {
  private val inFlight       = new AtomicInteger(0)
  private val closeRequested = new AtomicBoolean(false)
  private val released       = new AtomicBoolean(false)

  def save(): Array[Byte]
  def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double]
  def weights(): Array[Double]

  /** Frees the underlying native handle. Invoked exactly once, and only when no `whenNotClosed` block is running, so
    * implementations never race with an in-flight predict/save/weights call.
    */
  protected def releaseUnsafe(): Unit

  /** Requests the booster to be closed. Never blocks: if a `whenNotClosed` block is in flight on another thread, the
    * native release is deferred until the last such block exits. After this call, new `whenNotClosed` blocks fail.
    */
  final def close(): Unit = {
    closeRequested.set(true)
    if (inFlight.get() == 0) tryRelease()
  }

  final def isClosed(): Boolean = closeRequested.get()

  final def whenNotClosed[T](f: => T): T = {
    inFlight.incrementAndGet()
    try {
      if (closeRequested.get()) throw new IllegalStateException("booster is already closed")
      f
    } finally {
      if (inFlight.decrementAndGet() == 0 && closeRequested.get()) tryRelease()
    }
  }

  private def tryRelease(): Unit = if (released.compareAndSet(false, true)) releaseUnsafe()

  override def predict(values: RealMatrix): ArrayRealVector = {
    val rows = values.getRowDimension
    val cols = values.getColumnDimension
    val data = new Array[Double](rows * cols)
    var row  = 0
    while (row < values.getRowDimension) {
      System.arraycopy(values.getRow(row), 0, data, row * cols, cols)
      row += 1
    }
    new ArrayRealVector(predictMat(data, rows, cols))
  }

  override def predict(values: RealVector): Double = {
    predictMat(values.toArray, 1, values.getDimension)(0)
  }

  override def eval(data: Dataset, metric: Metric): Double = {
    val yhat = for {
      group <- data.groups
    } yield {
      predictMat(group.values, group.rows, group.columns)
    }
    val y = data.groups.map(_.labels)
    metric.eval(y.toArray, yhat.toArray)
  }

}

object Booster {
  case class DatasetOptions(categoryFeatures: Array[Int], dims: Int)
  trait BoosterOptions {
    def trees: Int
    def learningRate: Double
    def ndcgCutoff: Int
    def maxDepth: Int
    def randomSeed: Int
    def earlyStopping: Option[Int]
  }

  trait BoosterFactory[D, T <: Booster[D], O <: BoosterOptions] {
    def apply(string: Array[Byte]): T
    def formatData(ds: BoosterDataset, parent: Option[D], options: O): D
    def closeData(d: D): Unit = {}
    def train(dataset: D, test: Option[D], options: O, dso: DatasetOptions): T
  }
}
