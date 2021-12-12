package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.model.Dataset

case class BoosterDataset(
    original: Dataset,
    data: Array[Double],
    labels: Array[Double],
    groups: Array[Int],
    rows: Int,
    cols: Int,
    featureNames: Array[String]
)
