package me.dfdx.ltrlib.metric

object RMSE extends Metric {
  override def eval(y: Array[Array[Double]], yhat: Array[Array[Double]]): Double = math.sqrt(MSE.eval(y, yhat))
}
