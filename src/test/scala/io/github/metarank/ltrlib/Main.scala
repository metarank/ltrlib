package io.github.metarank.ltrlib

import better.files.File
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART
import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import io.github.metarank.ltrlib.booster.{LightGBMBooster, LightGBMOptions, XGBoostBooster, XGBoostOptions}
import io.github.metarank.ltrlib.input.LibsvmInputFormat
import io.github.metarank.ltrlib.metric.{Metric, NDCG}
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor, Model}
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import io.github.metarank.ltrlib.ranking.pointwise.{LogRegRanker, RandomRanker}
import io.github.metarank.ltrlib.ranking.pointwise.LogRegRanker.{BatchSGD, NoOptions}

object Main {
  val prefix = "/home/shutty/work/metarank/c14_ltr_challenge"
  def main(args: Array[String]): Unit = {
    val desc  = DatasetDescriptor((0 until 700).map(i => SingularFeature(s"f$i")).toList)
    val train = Dataset(desc, LibsvmInputFormat.load(File(s"$prefix/set2.train.txt").newInputStream, desc))
    val test  = Dataset(desc, LibsvmInputFormat.load(File(s"$prefix/set2.test.txt").newInputStream, desc))
    val xgb   = trainModel(LambdaMART(train, XGBoostOptions(), XGBoostBooster), NDCG(30), test)
    val lgbm  = trainModel(LambdaMART(train, LightGBMOptions(), LightGBMBooster), NDCG(30), test)
    val lr    = trainModel(LogRegRanker(train, BatchSGD(100, 1000, 0.01)), NDCG(30), test)
    val rand  = trainModel(RandomRanker(), NDCG(30), test)
    println(s"lgbm=$lgbm xgb=$xgb logreg=$lr rand=$rand")
  }

  def trainModel[M <: Model](ranker: Ranker[M], metric: Metric, test: Dataset) = {
    val model = ranker.fit()
    model.model.eval(test, metric)
  }
}
