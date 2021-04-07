package me.dfdx.ltrlib.booster

import me.dfdx.ltrlib.model.Dataset

case class BoosterDataset(
    original: Dataset,
    data: Array[Double],
    labels: Array[Double],
    groups: Array[Int],
    rows: Int,
    cols: Int
)
