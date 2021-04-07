package me.dfdx.ltrlib

import better.files.File
import me.dfdx.ltrlib.booster.{LightGBMBooster, XGBoostBooster}
import me.dfdx.ltrlib.input.LibsvmInputFormat
import me.dfdx.ltrlib.metric.{Metric, NDCG}
import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor}
import me.dfdx.ltrlib.ranking.Ranker
import me.dfdx.ltrlib.ranking.pairwise.LambdaMART
import me.dfdx.ltrlib.ranking.pointwise.{LogRegRanker, RandomRanker}
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker.{BatchSGD, NoOptions}

object Main {
  val prefix = "/home/shutty/work/metarank/c14_ltr_challenge"
  def main(args: Array[String]): Unit = {
    val desc  = DatasetDescriptor((0 until 700).map(i => SingularFeature(s"f$i")).toList)
    val train = Dataset(desc, LibsvmInputFormat(File(s"$prefix/set2.train.txt").newInputStream).load(desc))
    val test  = Dataset(desc, LibsvmInputFormat(File(s"$prefix/set2.test.txt").newInputStream).load(desc))

    val xgb  = trainModel(LambdaMART(train, XGBoostBooster.apply), NDCG(30), test)
    val lgbm = trainModel(LambdaMART(train, LightGBMBooster.apply), NDCG(30), test)
    val lr   = trainModel(LogRegRanker(train, BatchSGD(100, 1000)), NDCG(30), test)
    val rand = trainModel(RandomRanker(), NDCG(30), test)
    println(s"lgbm=$lgbm xgb=$xgb logreg=$lr rand=$rand")
  }

  def trainModel[M](ranker: Ranker[M], metric: Metric, test: Dataset) = {
    val model = ranker.fit()
    ranker.eval(model, test, metric)
  }
}
