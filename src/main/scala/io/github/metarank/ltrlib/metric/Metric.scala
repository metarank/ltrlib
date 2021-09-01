package io.github.metarank.ltrlib.metric

trait Metric {

  /** Eval metric for multiple query groups
    * @param y ground truth
    * @param yhat predicted values
    * @return
    */
  def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double
}
