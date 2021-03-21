# libLTR: a Learn-to-Rank algorithm library

[![CI Status](https://github.com/metarank/libltr/workflows/CI/badge.svg)](https://github.com/metarank/libltr/actions)
[![License: Apache 2](https://img.shields.io/badge/License-Apache2-green.svg)](https://opensource.org/licenses/Apache-2.0)

A Java/Scala library to wrap and implement basic learn-to-rank ML algorithms under the same
human-friendly API. Currently is under an active development.

### Supported features

* Logistic regression ranking
* MSE/RMSE loss

### Roadmap
* NDCG, MAP metric
* wrap LightGBM and XGBoost implementations of LambdaMART
* wrap RankLib family of algorithms
* Plackett-Luce weighting for cascade model
* Reinforcement learning: LinUCB, hLinUCB, CPR
* dataset support: [webscope R6B, C14](https://webscope.sandbox.yahoo.com/catalog.php?datatype=r)
* unbiased algorithm evaluation: Li, IPW

## License

This project is released under the Apache 2.0 license, as specified in the LICENSE file.