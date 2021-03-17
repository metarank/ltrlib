package me.dfdx.ltrlib.model

case class DatasetDescriptor(offsets: Map[Feature, Int], features: List[Feature], dim: Int) {}

object DatasetDescriptor {
  def apply(features: List[Feature]) = {
    val offsets = features
      .map(_.size)
      .scanLeft(0)(_ + _)
      .zip(features)
      .map(_.swap)
    val nonUnique = features.map(_.name).groupBy(identity).filter(_._2.size > 1).keys.toList
    if (nonUnique.nonEmpty) throw new IllegalArgumentException(s"non-unique feature names: $nonUnique")
    new DatasetDescriptor(
      offsets = offsets.toMap,
      features = features,
      dim = features.map(_.size).sum
    )
  }
}
