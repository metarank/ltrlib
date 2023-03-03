package io.github.metarank.ltrlib.booster

import Booster.{BoosterFactory, BoosterOptions, DatasetOptions}
import io.github.metarank.lightgbm4j.LGBMDataset
import io.github.metarank.ltrlib.booster.XGBoostBooster.BITSTREAM_VERSION
import io.github.metarank.ltrlib.util.Logging
import ml.dmlc.xgboost4j.java.{DMatrix, IObjective, XGBoost}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}
import java.util.Base64
import scala.jdk.CollectionConverters._

case class XGBoostBooster(
    model: ml.dmlc.xgboost4j.java.Booster,
    featureTypes: Array[String]
) extends Booster[DMatrix]
    with Logging {

  override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = {
    val mat = new DMatrix(values.map(_.toFloat), rows, cols, Float.NaN)
    mat.setGroup(Array(rows))
    mat.setFeatureTypes(featureTypes)
    val result = model.predict(mat)
    val out    = new Array[Double](rows)
    var i      = 0
    while (i < rows) {
      out(i) = result(i)(0)
      i += 1
    }
    out
  }

  override def close(): Unit = model.dispose()

  override def save(): Array[Byte] = {
    val bytes = new ByteArrayOutputStream()
    val data  = new DataOutputStream(bytes)
    data.writeByte(BITSTREAM_VERSION)
    data.writeInt(featureTypes.length)
    featureTypes.foreach(ft => data.writeUTF(ft))
    val modelStream = new ByteArrayOutputStream()
    model.saveModel(modelStream, "json")
    val modelBytes = modelStream.toByteArray
    data.writeInt(modelBytes.length)
    data.write(modelBytes)
    data.close()
    bytes.toByteArray
  }

  override def weights(): Array[Double] = {
    val names   = (0 until model.getNumFeature.toInt).map(i => s"feature$i").toArray
    val weights = model.getFeatureScore(names).asScala
    val result = for {
      name <- names
    } yield {
      weights.get(name).map(_.doubleValue()).getOrElse(0.0)
    }
    result
  }
}

object XGBoostBooster extends BoosterFactory[DMatrix, XGBoostBooster, XGBoostOptions] with Logging {
  val BITSTREAM_VERSION = 2

  override def apply(string: Array[Byte]): XGBoostBooster = {
    val stream  = new DataInputStream(new ByteArrayInputStream(string))
    val version = stream.readByte().toInt
    version match {
      case BITSTREAM_VERSION =>
        val featureTypesSize = stream.readInt()
        val featureTypes     = (0 until featureTypesSize).map(_ => stream.readUTF()).toArray
        val boosterSize      = stream.readInt()
        val buffer           = new Array[Byte](boosterSize)
        stream.readFully(buffer)
        val booster = XGBoost.loadModel(buffer)
        XGBoostBooster(booster, featureTypes)
      case _ => throw new Exception("you use old binary xgboost serialization format, please re-serialize")
    }
  }

  override def formatData(d: BoosterDataset, parent: Option[DMatrix]): DMatrix = {
    val mat = new DMatrix(d.data.map(_.toFloat), d.rows, d.cols, Float.NaN)
    mat.setLabel(d.labels.map(_.toFloat))
    mat.setGroup(d.groups)
    val ftypes = new Array[String](d.original.desc.dim)
    var i      = 0
    while (i < d.original.desc.dim) {
      ftypes(i) = if (d.categoricalIndices.contains(i)) "c" else "q"
      i += 1
    }
    mat.setFeatureTypes(ftypes)
    mat
  }

  override def train(
      dataset: DMatrix,
      test: Option[DMatrix],
      options: XGBoostOptions,
      dso: DatasetOptions
  ): XGBoostBooster = {
    val opts = Map[String, Object](
      "objective"   -> "rank:pairwise",
      "eval_metric" -> s"ndcg@${options.ndcgCutoff}",
      "num_round"   -> Integer.valueOf(options.trees),
      "max_depth"   -> options.maxDepth.toString,
      "eta"         -> options.learningRate.toString,
      "seed"        -> options.randomSeed.toString,
      "subsample"   -> options.subsample.toString,
      "tree_method" -> options.treeMethod
    ).asJava
    val model: ml.dmlc.xgboost4j.java.Booster = XGBoost.train(dataset, opts, 0, Map.empty.asJava, null, null)

    var it           = 0
    var earlyStop    = false
    var lastBest     = 0.0
    var lastBestIter = 0
    while ((it < options.trees) && !earlyStop) {
      it += 1
      model.update(dataset, 1)
      val ndcgTrain = evalMetric(model, dataset)
      test match {
        case Some(value) =>
          val ndcgTest = evalMetric(model, value)
          logger.info(
            s"[$it] NDCG@${options.ndcgCutoff}:train = $ndcgTrain NDCG@${options.ndcgCutoff}:test = $ndcgTest"
          )
          options.earlyStopping match {
            case Some(esThreshold) =>
              if (ndcgTest > lastBest) {
                lastBest = ndcgTest
                lastBestIter = it
              }
              if ((it - lastBestIter) > esThreshold) {
                logger.info(s"early stop: $esThreshold rounds passed, best=$lastBest last=$ndcgTest")
                earlyStop = true
              }
            case None => //
          }
        case None =>
          logger.info(s"[$it] NDCG@train = $ndcgTrain")
      }
    }
    val ftypes = (0 until dso.dims).map(x => if (dso.categoryFeatures.contains(x)) "c" else "q").toArray
    XGBoostBooster(model, ftypes)
  }

  override def closeData(d: DMatrix): Unit = d.dispose()

  def evalMetric(model: ml.dmlc.xgboost4j.java.Booster, dataset: DMatrix): Double = {
    val result = model.evalSet(Array(dataset), Array("test"), 1)
    result.split(':').last.toDouble
  }

}
