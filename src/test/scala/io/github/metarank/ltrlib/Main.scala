package io.github.metarank.ltrlib

import better.files.File
import io.github.metarank.ltrlib.ranking.Ranker
import io.github.metarank.ltrlib.ranking.pairwise.LambdaMART
import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import io.github.metarank.ltrlib.booster.{LightGBMBooster, XGBoostBooster}
import io.github.metarank.ltrlib.input.LibsvmInputFormat
import io.github.metarank.ltrlib.metric.{Metric, NDCG}
import io.github.metarank.ltrlib.model.{Dataset, DatasetDescriptor}
import io.github.metarank.ltrlib.model.Feature.SingularFeature
import io.github.metarank.ltrlib.ranking.pointwise.{LogRegRanker, RandomRanker}
import io.github.metarank.ltrlib.ranking.pointwise.LogRegRanker.{BatchSGD, NoOptions}

object Main {
  val prefix = "/home/shutty/work/metarank/c14_ltr_challenge"
  def main(args: Array[String]): Unit = {
    val desc  = DatasetDescriptor((0 until 700).map(i => SingularFeature(s"f$i")).toList)
    val train = Dataset(desc, LibsvmInputFormat(File(s"$prefix/set2.train.txt").newInputStream).load(desc))
    val test  = Dataset(desc, LibsvmInputFormat(File(s"$prefix/set2.test.txt").newInputStream).load(desc))
    val opts  = BoosterOptions()
    val xgb   = trainModel(LambdaMART(train, opts, XGBoostBooster.apply), NDCG(30), test)
    val lgbm  = trainModel(LambdaMART(train, opts, LightGBMBooster.apply), NDCG(30), test)
    val lr    = trainModel(LogRegRanker(train, BatchSGD(100, 1000)), NDCG(30), test)
    val rand  = trainModel(RandomRanker(), NDCG(30), test)
    println(s"lgbm=$lgbm xgb=$xgb logreg=$lr rand=$rand")
  }

  def trainModel[M](ranker: Ranker[M], metric: Metric, test: Dataset) = {
    val model = ranker.fit()
    ranker.eval(model, test, metric)
  }
}
