package me.dfdx.ltrlib.dataset

import me.dfdx.ltrlib.model.Feature.SingularFeature
import me.dfdx.ltrlib.model.{Dataset, DatasetDescriptor, LabeledItem, Query}

object SmallDiabetesDataset {
  val desc = DatasetDescriptor(List(SingularFeature("one"), SingularFeature("two")))
  def apply() = Dataset(
    desc,
    List(
      Query(
        desc,
        List(
          LabeledItem(0, 1, Array(2.7810836, 2.550537003)),
          LabeledItem(0, 1, Array(1.465489372, 2.362125076)),
          LabeledItem(0, 1, Array(3.396561688, 4.400293529)),
          LabeledItem(0, 1, Array(1.38807019, 1.850220317)),
          LabeledItem(0, 1, Array(3.06407232, 3.005305973)),
          LabeledItem(1, 1, Array(7.627531214, 2.759262235)),
          LabeledItem(1, 1, Array(5.332441248, 2.088626775)),
          LabeledItem(1, 1, Array(6.922596716, 1.77106367)),
          LabeledItem(1, 1, Array(8.675418651, -0.242068655)),
          LabeledItem(1, 1, Array(7.673756466, 3.508563011))
        )
      )
    )
  )

}
