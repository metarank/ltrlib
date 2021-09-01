package io.github.metarank.ltrlib.model

case class Dataset(desc: DatasetDescriptor, groups: List[Query]) {
  lazy val itemCount = groups.map(_.labels.length).sum
}
