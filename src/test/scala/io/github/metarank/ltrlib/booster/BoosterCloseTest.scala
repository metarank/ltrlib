package io.github.metarank.ltrlib.booster

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class BoosterCloseTest extends AnyFlatSpec with Matchers {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  /** A booster whose predict blocks until `proceed` is counted down, so a close can be raced against it. */
  class BlockingBooster extends Booster[Unit] {
    val releases   = new AtomicInteger(0)
    val entered    = new CountDownLatch(1)
    val proceed    = new CountDownLatch(1)
    var failInside = false

    override def save(): Array[Byte]      = whenNotClosed(Array.emptyByteArray)
    override def weights(): Array[Double] = whenNotClosed(Array.emptyDoubleArray)
    override def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double] = whenNotClosed {
      entered.countDown()
      proceed.await(10, TimeUnit.SECONDS)
      if (failInside) throw new RuntimeException("boom")
      Array.fill(rows)(1.0)
    }
    override protected def releaseUnsafe(): Unit = releases.incrementAndGet()
  }

  def predictAsync(b: BlockingBooster, rows: Int = 1): Future[Array[Double]] =
    Future(b.predictMat(Array.fill(rows)(0.0), rows, 1))

  it should "release immediately when nothing is in flight" in {
    val b = new BlockingBooster()
    b.isClosed() shouldBe false
    b.close()
    b.isClosed() shouldBe true
    b.releases.get() shouldBe 1
  }

  it should "defer the release until an in-flight predict completes" in {
    val b = new BlockingBooster()
    val f = predictAsync(b, rows = 3)
    b.entered.await(10, TimeUnit.SECONDS) shouldBe true

    b.close()
    b.isClosed() shouldBe true
    b.releases.get() shouldBe 0

    b.proceed.countDown()
    Await.result(f, 10.seconds).toList shouldBe List(1.0, 1.0, 1.0)
    b.releases.get() shouldBe 1
  }

  it should "reject calls after close and not release twice" in {
    val b = new BlockingBooster()
    b.close()
    b.proceed.countDown()
    an[IllegalStateException] should be thrownBy b.predictMat(Array(0.0), 1, 1)
    an[IllegalStateException] should be thrownBy b.save()
    an[IllegalStateException] should be thrownBy b.weights()
    b.releases.get() shouldBe 1
  }

  it should "release only after all concurrent predicts finish" in {
    val b       = new BlockingBooster()
    val entered = new CountDownLatch(2)
    val f1      = Future(b.whenNotClosed { entered.countDown(); b.proceed.await(10, TimeUnit.SECONDS); 1 })
    val f2      = Future(b.whenNotClosed { entered.countDown(); b.proceed.await(10, TimeUnit.SECONDS); 2 })
    entered.await(10, TimeUnit.SECONDS) shouldBe true

    b.close()
    b.releases.get() shouldBe 0

    b.proceed.countDown()
    Await.result(f1, 10.seconds) shouldBe 1
    Await.result(f2, 10.seconds) shouldBe 2
    b.releases.get() shouldBe 1
  }

  it should "still release when the in-flight call throws" in {
    val b = new BlockingBooster()
    b.failInside = true
    val f = predictAsync(b)
    b.entered.await(10, TimeUnit.SECONDS) shouldBe true

    b.close()
    b.releases.get() shouldBe 0

    b.proceed.countDown()
    a[RuntimeException] should be thrownBy Await.result(f, 10.seconds)
    b.releases.get() shouldBe 1
  }

  it should "release once on double close" in {
    val b = new BlockingBooster()
    b.close()
    b.close()
    b.releases.get() shouldBe 1
  }

  it should "release exactly once under a close/predict race" in {
    // hammer the protocol: many predicts racing a close, release must happen exactly once and never mid-call
    for (_ <- 1 to 200) {
      val b = new BlockingBooster()
      b.proceed.countDown()
      val active = new AtomicInteger(0)
      val fs = (1 to 8).map(_ =>
        Future {
          try {
            b.whenNotClosed {
              active.incrementAndGet()
              b.releases.get() shouldBe 0
              active.decrementAndGet()
            }
          } catch { case _: IllegalStateException => () }
        }
      )
      b.close()
      Await.result(Future.sequence(fs), 10.seconds)
      b.releases.get() shouldBe 1
    }
  }
}
