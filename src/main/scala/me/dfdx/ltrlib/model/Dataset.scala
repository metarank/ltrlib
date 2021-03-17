package me.dfdx.ltrlib.model

case class Dataset(desc: DatasetDescriptor, groups: List[Query]) {
  lazy val itemCount = groups.map(_.labels.length).sum
}
