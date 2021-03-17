package me.dfdx.ltrlib.metric

object MSE extends Metric {
  override def eval(y: Double, yhat: Double): Double = (y - yhat) * (y - yhat)

  override def gradient(y: Double, yhat: Double): Double = ???
}
