# libLTR: a Learn-to-Rank algorithm library

[![CI Status](https://github.com/metarank/ltrlib/workflows/CI/badge.svg)](https://github.com/metarank/ltrlib/actions)
[![License: Apache 2](https://img.shields.io/badge/License-Apache2-green.svg)](https://opensource.org/licenses/Apache-2.0)
![Last release](https://img.shields.io/github/release/metarank/ltrlib)

A Java/Scala library to wrap and implement basic learn-to-rank ML algorithms under the same
human-friendly API. Currently, is under an active development.

### Supported features

* Logistic regression ranking: SGD, Batch SGD
* MSE/RMSE loss
* LambdaMART: XGBoost, LightGBM and Catboost backends
* NDCG, MAP metrics
* Data formats: libSVM 
* Native categorical features

## Installation

libLTR is published to maven-central for scala 3.x, 2.12 and 2.13, so for SBT, add this snippet to `build.sbt`:
```scala
libraryDependencies += "io.github.metarank" %% "ltrlib" % "0.2.2"
```

For maven:
```xml
<dependency>
  <groupId>io.github.metarank</groupId>
  <artifactId>ltrlib_2.13</artifactId>
  <version>0.2.2</version>
</dependency>
```
## Usage

Get LETOR dataset from [https://github.com/dmlc/xgboost/tree/master/demo/rank](https://github.com/dmlc/xgboost/tree/master/demo/rank), then:
```scala

val loader  = LibsvmInputFormat(new GZIPInputStream(new FileInputStream("<path_to_file.gz>")))
// a dataset descriptor, if you want to have access to feature metadata like names
val spec    = DatasetDescriptor((1 to 46).map(i => SingularFeature(s"f$i")).toList)
// the dataset itself
val dataset = Dataset(spec, loader.load(spec))

// configured booster 
val lm      = LambdaMART(dataset, LightGBMBooster)
// trained model
val model   = lm.fit(LightGBMOptions())
// NDCG error with cutoff on 10th position
val error   = lm.eval(model, dataset, NDCG(10))

```

### Roadmap
* wrap RankLib family of algorithms
* Plackett-Luce weighting for cascade model
* Reinforcement learning: LinUCB, hLinUCB, CPR
* dataset support: [webscope R6B, C14](https://webscope.sandbox.yahoo.com/catalog.php?datatype=r)
* unbiased algorithm evaluation: Li, IPW

## License

This project is released under the Apache 2.0 license, as specified in the LICENSE file.
