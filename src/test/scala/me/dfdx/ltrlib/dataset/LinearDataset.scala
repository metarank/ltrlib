package me.dfdx.ltrlib.dataset

import me.dfdx.ltrlib.dataset.SmallDiabetesDataset.desc
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}
import me.dfdx.ltrlib.model.Feature.SingularFeature

object LinearDataset {
  val desc = DatasetDescriptor(List(SingularFeature("one"), SingularFeature("two")))
  def apply() = Dataset(
    desc,
    List(
      Query(
        desc,
        List(
          LabeledItem(0, 1, Array(0.1, 2.55)),
          LabeledItem(0, 1, Array(0.2, 2.36)),
          LabeledItem(0, 1, Array(0.15, 4.40)),
          LabeledItem(0, 1, Array(0.09, 1.85)),
          LabeledItem(0, 1, Array(0.08, 3.00)),
          LabeledItem(1, 1, Array(0.75, 2.75)),
          LabeledItem(1, 1, Array(0.89, 2.08)),
          LabeledItem(1, 1, Array(0.99, 1.77)),
          LabeledItem(1, 1, Array(0.95, 0.24)),
          LabeledItem(1, 1, Array(0.81, 3.50))
        )
      )
    )
  )
}
