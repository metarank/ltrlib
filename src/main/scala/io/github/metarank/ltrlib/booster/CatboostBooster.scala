package io.github.metarank.ltrlib.booster

import ai.catboost.CatBoostModel
import better.files.File
import io.circe.Decoder
import io.github.metarank.ltrlib.booster.Booster.BoosterFactory
import io.circe.generic.semiauto._
import io.github.metarank.ltrlib.booster.CatboostBooster.CatTrainJson.{Iteration, Meta}
import io.github.metarank.ltrlib.output.LibSVMOutputFormat
import ru.yandex.catboost.spark.catboost4j_spark.core.src.native_impl.{TVector_TString, native_impl}

import java.io.ByteArrayInputStream

case class CatboostBooster(booster: CatBoostModel, bytes: Array[Byte]) extends Booster[String] {
  override def save(): Array[Byte] = bytes

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

  case class CatTrainJson(meta: Meta, iterations: List[Iteration])

  object CatTrainJson {
    case class Meta(test_sets: List[String], test_metrics: List[TestMetric], learn_metrics: List[TestMetric])

    case class TestMetric(best_value: String, name: String)

    case class Iteration(
        learn: List[Double],
        passed_time: Double,
        remaining_time: Double,
        iteration: Int,
        test: List[Double]
    )

    implicit val metaDecoder: Decoder[Meta]       = deriveDecoder[Meta]
    implicit val tmDecoder: Decoder[TestMetric]   = deriveDecoder[TestMetric]
    implicit val itDecoder: Decoder[Iteration]    = deriveDecoder[Iteration]
    implicit val ctDecoder: Decoder[CatTrainJson] = deriveDecoder[CatTrainJson]
  }

  override def apply(string: Array[Byte]): CatboostBooster = {
    val cb = CatBoostModel.loadModel(new ByteArrayInputStream(string))
    CatboostBooster(cb, string)
  }

  override def formatData(ds: BoosterDataset, parent: Option[String]): String = {
    val file   = File.newTemporaryFile("catboost-", ".svm")
    val stream = file.newFileOutputStream()
    LibSVMOutputFormat.write(stream, ds.original, 1)
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
      "--logging-level" -> "Silent",
      "--random-seed"   -> options.randomSeed.toString
    ) ++ test.map(t => Map("--test-set" -> t)).getOrElse(Map.empty)
    native_impl.ModeFitImpl(new TVector_TString(opts.flatMap(kv => List(kv._1, kv._2)).toArray))
    val bytes = modelFile.byteArray
    apply(bytes)
  }
}
