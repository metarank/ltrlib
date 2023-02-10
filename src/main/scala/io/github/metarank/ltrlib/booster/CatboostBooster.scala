package io.github.metarank.ltrlib.booster

import ai.catboost.CatBoostModel
import better.files.File
import io.github.metarank.ltrlib.booster.Booster.BoosterFactory
import io.github.metarank.ltrlib.output.LibSVMOutputFormat
import ru.yandex.catboost.spark.catboost4j_spark.core.src.native_impl.{TVector_TString, native_impl}

import java.io.ByteArrayInputStream

case class CatboostBooster(booster: CatBoostModel, bytes: Array[Byte]) extends Booster[String] {
  override def save(): Array[Byte] = bytes

  override def close(): Unit = booster.close()

  override def weights(): Array[Double] = Array.emptyDoubleArray

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    val split = new Array[Array[Float]](rows)
    var i     = 0
    while (i < rows) {
      val row = new Array[Float](cols)
      var j   = 0
      while (j < cols) {
        row(j) = values(i * cols + j).toFloat
        j += 1
      }
      split(i) = row
      i += 1
    }
    val cats: Array[Array[Int]] = null
    val preds                   = booster.predict(split, cats)
    val result                  = new Array[Double](rows)
    var k                       = 0
    while (k < rows) {
      result(k) = preds.get(k, 0)
      k += 1
    }
    result
  }

}

object CatboostBooster extends BoosterFactory[String, CatboostBooster, CatboostOptions] {

  override def apply(string: Array[Byte]): CatboostBooster = {
    val cb = CatBoostModel.loadModel(new ByteArrayInputStream(string))
    CatboostBooster(cb, string)
  }

  override def formatData(ds: BoosterDataset, parent: Option[String]): String = {
    val file   = File.newTemporaryFile("catboost-", ".svm")
    val stream = file.newFileOutputStream()
    LibSVMOutputFormat.write(stream, ds.original)
    stream.close()
    s"libsvm://$file"
  }

  override def train(
      dataset: String,
      test: Option[String],
      options: CatboostOptions,
      dso: Booster.DatasetOptions
  ): CatboostBooster = {
    val dir       = File.newTemporaryDirectory("catboost-train").deleteOnExit()
    val modelFile = dir.createChild("model.bin")

    val opts = Map(
      "--learn-set"     -> dataset,
      "--loss-function" -> options.objective,
      "--eval-metric"   -> s"NDCG:top=${options.ndcgCutoff}",
      "--iterations"    -> options.trees.toString,
      "--depth"         -> options.maxDepth.toString,
      "--learning-rate" -> options.learningRate.toString,
      "--train-dir"     -> dir.toString(),
      "--model-file"    -> modelFile.toString(),
      "--logging-level" -> options.loggingLevel,
      "--random-seed"   -> options.randomSeed.toString
    ).flatMap(kv => List(kv._1, kv._2))
    val testOpts = test match {
      case Some(value) => List("--test-set", value)
      case None        => Nil
    }
    val earlyStopOpts = options.earlyStopping match {
      case Some(value) => List("--od-type", "Iter", "--od-wait", value.toString)
      case None        => Nil
    }
    val finalOpts = List.concat(opts, testOpts, earlyStopOpts)
    native_impl.ModeFitImpl(new TVector_TString(finalOpts.toArray))
    val bytes = modelFile.byteArray
    apply(bytes)
  }
}
