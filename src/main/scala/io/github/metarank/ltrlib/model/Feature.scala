package io.github.metarank.ltrlib.model

sealed trait Feature {
  def name: String
  def size: Int
}

object Feature {
  case class SingularFeature(name: String) extends Feature {
    override lazy val size = 1
  }
  case class VectorFeature(name: String, size: Int) extends Feature
}
