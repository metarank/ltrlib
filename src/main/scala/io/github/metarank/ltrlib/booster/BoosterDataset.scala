package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.model.{Dataset, Feature}

case class BoosterDataset(
    original: Dataset,
    data: Array[Double],
    labels: Array[Double],
    groups: Array[Int],
    positions: Array[Int],
    rows: Int,
    cols: Int,
    featureNames: Array[String]
) {
  lazy val categories         = original.desc.features.collect { case c: Feature.CategoryFeature => c }
  lazy val categoriesStrings  = categories.map(_.name).toArray
  lazy val categoricalIndices = categories.flatMap(c => original.desc.offsets.get(c)).toArray
}
