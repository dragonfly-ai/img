package ai.dragonfly.img

import ai.dragonfly.color._
import ai.dragonfly.distributed.Snowflake

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportAll}
import scala.scalajs.js.typedarray.Uint8ClampedArray
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

@JSExport
class ImgAsync (@JSExport val width: Int, private var pixelData: Uint8ClampedArray) {

  @JSExport val height: Int = (pixelData.length / 4) / width

  private var checkedIn = true

  @JSExport def this(img:Img) = this(img.width, img.copy().asInstanceOf[Img].pixelData)

  @JSExport def this(width: Int, height: Int) = this(width, new Uint8ClampedArray(width * height * 4))

  @JSExport def this(width: Int, pixelArray: Array[Int]) = this(
    width,
    {
      val ui8ca = new Uint8ClampedArray(pixelArray.length * 4)
      var j = 0
      for (i: Int <- 0 until pixelArray.length) {
        j = i * 4
        val c: RGBA = pixelArray(i)
        ui8ca(j) = c.red
        ui8ca(j+1) = c.green
        ui8ca(j+2) = c.blue
        ui8ca(j+3) = c.alpha
      }
      ui8ca
    }
  )

  def checkInPixelData(pd: Uint8ClampedArray = this.pixelData): Unit = {
    //println("restorePixelData")
    this.pixelData = pd
    this.checkedIn = true
    processQueue()
  }

  val promiseQueue: mutable.Queue[Promise[Uint8ClampedArray]] = new mutable.Queue[Promise[Uint8ClampedArray]]()

  private def processQueue(): Unit = {
    //println("processQueue()")
    if (promiseQueue.nonEmpty) {
      //println("\tpromiseQueue not empty.")
      val promise = promiseQueue.dequeue()
      this.checkedIn = false
      promise.success(this.pixelData)
    } else {
      //println("\tpromiseQueue empty.")
    }
  }

  // low level
  def checkOutPixelData: Future[Uint8ClampedArray] = {
    //println("checkOutPixelData")
    if (this.checkedIn && promiseQueue.isEmpty) {
      //println("\tProvided right away.")
      this.checkedIn = false
      Future[Uint8ClampedArray] { pixelData }
    } else {  // provide the pixelData immediately
      //println("\tUse the queue.")
      // defer the provision of pixelData until after the worker returns it to the main thread
      val promise = Promise[Uint8ClampedArray]
      promiseQueue.enqueue(promise)
      promise.future
    }
  }

  def awaitPixelData[T](f: (Uint8ClampedArray) => T ): Future[T] = {
    val promise = Promise[T]
    checkOutPixelData onComplete {
      case Success(pd) =>
        promise.success(f(pd))
        processQueue()
      case Failure(t) => promise.failure(new LostPixelDataException)
    }
    promise.future
  }

  private def getARGB(pd: Uint8ClampedArray, x:Int, y:Int): Int = {
    val index = linearIndexOf(x, y)
    RGBA(pd(index), pd(index + 1), pd(index + 2), pd(index + 3)).argb
  }

  @JSExport def getARGB(x:Int, y:Int): Future[Int] = awaitPixelData[Int] (
    (pd: Uint8ClampedArray) => getARGB(pd, x, y)
  )

  private def setARGB(pd: Uint8ClampedArray, x:Int, y:Int, argb: Int): Unit = {
    val c: RGBA = argb
    val index = linearIndexOf(x, y)
    pd(index) = c.red
    pd(index + 1) = c.green
    pd(index + 2) = c.blue
    pd(index + 3) = c.alpha
  }

  @JSExport def setARGB(x:Int, y:Int, argb: Int): Unit = awaitPixelData[Unit] (
    (pd: Uint8ClampedArray) => setARGB(pd, x, y, argb)
  )

  // not dependent on pixel data.
  def linearIndexOf(x: Int, y: Int): Int = (y * width + x) * 4



  @JSExport def getIntArray(startX: Int, startY: Int, w: Int, h: Int): Future[Array[Int]] = awaitPixelData[Array[Int]] (
    (pd: Uint8ClampedArray) => {
      val arr = new Array[Int](w * h)
      var i = 0
      for (y <- 0 until h) {
        for (x <- 0 until w) {
          val index = linearIndexOf(x, y)
          arr(i) = RGBA(pd(index), pd(index + 1), pd(index + 2), pd(index + 3))
          i = i + 1
        }
      }
      arr
    }
  )

  @JSExport def setIntArray(startX: Int, startY: Int, w: Int, h: Int, rgba: Array[Int], offset: Int, stride: Int): Future[ImgAsync] = awaitPixelData[ImgAsync] (
    (pd: Uint8ClampedArray) => {
      var workingOffset = offset

      for (y <- startY until startY + h) {
        var xOffset = workingOffset
        for (x <- startX until startX + w) {
          val argb = rgba(xOffset)
          xOffset = xOffset + 1
          setARGB(pd, x, y, argb)
        }
        workingOffset = workingOffset + stride
      }

      this
    }
  )


  private def getUint8ClampedArray(pd: Uint8ClampedArray, startX: Int, startY: Int, w: Int, h: Int): Uint8ClampedArray = {
    val arr = new Uint8ClampedArray(w * h * 4)
    var i = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        val c: RGBA = getARGB(pd, startX + x, startY + y)
        arr(i) = c.red
        arr(i+1) = c.green
        arr(i+2) = c.blue
        arr(i+3) = c.alpha
        i = i + 4
      }
    }
    arr
  }

  @JSExport def getUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int): Future[Uint8ClampedArray] = awaitPixelData[Uint8ClampedArray] (
    (pd: Uint8ClampedArray) => getUint8ClampedArray(pd, startX, startY, w, h)
  )

  @JSExport  def setUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int, uint8Array: Uint8ClampedArray): Future[ImgAsync] = awaitPixelData[ImgAsync] (
    (pd: Uint8ClampedArray) => {
      var workingOffset = 0

      for (y <- startY until startY + h) {
        for (x <- startX until startX + w) {
          val argb = RGBA(
            uint8Array(workingOffset),
            uint8Array(workingOffset + 1),
            uint8Array(workingOffset + 2),
            uint8Array(workingOffset + 3)
          ).argb
          workingOffset = workingOffset + 4
          setARGB(pd, x, y, argb)
        }
      }

      this
    }
  )

  @JSExport def asUint8ClampedArray: Future[Uint8ClampedArray] = checkOutPixelData

  @JSExport def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): Future[ImgAsync] = awaitPixelData[ImgAsync] (
    (pd: Uint8ClampedArray) => new ImgAsync(w, getUint8ClampedArray(pd, xOffset, yOffset, w, h))
  )

  @JSExport def copy(): ImgAsync = {
    val cpy = new ImgAsync(width, height)
    for {
      pdC : Uint8ClampedArray <- cpy.checkOutPixelData
      pd : Uint8ClampedArray <- checkOutPixelData
    } yield {
      for (i <- 0 until pd.length) {
        pdC(i) = pd(i)
      }
      checkInPixelData(pd)
      cpy.checkInPixelData()
    }
    cpy
  }

  @Override override def toString(): String = s"Img($width X $height)"

}

class LostPixelDataException extends Exception("Could not procure Pixel Data.")