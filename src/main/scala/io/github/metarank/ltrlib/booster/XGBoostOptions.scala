package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.booster.Booster.BoosterOptions

import scala.util.Random

case class XGBoostOptions(
    trees: Int = 100,
    learningRate: Double = 0.1,
    ndcgCutoff: Int = 10,
    maxDepth: Int = 8,
    randomSeed: Int = math.abs(Random.nextInt()),
    subsample: Double = 1.0,
    earlyStopping: Option[Int] = None
) extends BoosterOptions
