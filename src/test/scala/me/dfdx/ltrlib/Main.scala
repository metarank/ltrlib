package me.dfdx.ltrlib

import better.files.File
import me.dfdx.ltrlib.input.LibsvmInputFormat
import me.dfdx.ltrlib.metric.NDCG
import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor}
import me.dfdx.ltrlib.ranking.pairwise.LambdaMART
import me.dfdx.ltrlib.ranking.pairwise.LambdaMART.LightGBMBoosterOptions
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker
import me.dfdx.ltrlib.ranking.pointwise.LogRegRanker.{BatchSGD, NoOptions}

object Main {
  val prefix = "/home/shutty/work/metarank/c14_ltr_challenge"
  def main(args: Array[String]): Unit = {
    val desc       = DatasetDescriptor((0 until 700).map(i => SingularFeature(s"f$i")).toList)
    val train      = Dataset(desc, LibsvmInputFormat(File(s"$prefix/set2.train.txt").newInputStream).load(desc))
    val test       = Dataset(desc, LibsvmInputFormat(File(s"$prefix/set2.test.txt").newInputStream).load(desc))
    val booster    = LambdaMART(train)
    val model      = booster.fit(LightGBMBoosterOptions(100))
    val trainScore = booster.eval(model, train, NDCG(30))
    val testScore  = booster.eval(model, test, NDCG(30))
    val logreg     = LogRegRanker(train)
    val lmod       = logreg.fit(BatchSGD(100, 1000))
    val lts        = logreg.eval(lmod, train, NDCG(30))
    val lvs        = logreg.eval(lmod, test, NDCG(30))
    println(s"lmart test=$testScore train=$trainScore, lr test=$lvs train=$lts")
  }
}
