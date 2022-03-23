package ai.dragonfly.img.async

import ai.dragonfly.img.Img

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class AsyncImg(private var img:Img) {

  val width: Int = img.width
  val height: Int = img.height

  // Constructors:
  def this(width: Int, height: Int) = this ( Img(width, height) )

  private var checkedIn = true
  private val promiseQueue: mutable.Queue[Promise[Img]] = new mutable.Queue[Promise[Img]]()

  private def processQueue(): Unit = {
    println("processQueue()")
    if (promiseQueue.nonEmpty) {
      println("\tpromiseQueue not empty.")
      val promise = promiseQueue.dequeue()
      this.checkedIn = false
      promise.success(this.img)
    } else {
      println("\tpromiseQueue empty.")
    }
  }

  private def checkOutImgData: Future[Img] = {
    println("checkOutImgData")
    if (this.checkedIn && promiseQueue.isEmpty) { // provide the pixelData immediately
      println("\tProvided right away.")
      this.checkedIn = false
      Future[Img] { this.img }
    } else {  // enqueue this request for image data.
      println("\tUse the queue.")
      val promise = Promise[Img]()
      promiseQueue.enqueue(promise)
      promise.future
    }
  }

  private def checkInImgData(img: Img): Unit = {
    println("checkInImgData")
    this.img = img
    this.checkedIn = true
    processQueue()
  }

  def reserveImgData[T](f: Img => T ): Future[T] = {
    println("called reserveImgData")
    val promise = Promise[T]()
    checkOutImgData onComplete {
      case Success(img: Img) =>
        println("\tinvoking callback.")
        println(s"f = $f")
        val rv:T = f(img)
        println(rv)
        println("\tcompleted callback.")
        promise.success(rv)
        checkInImgData(img)
      case Failure(t) => promise.failure(new LostPixelDataException)
    }
    promise.future
  }

  def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): AsyncImg = {
    val asyncImg = new AsyncImg(w, h)
    asyncImg.checkedIn = false

    reserveImgData[Unit]( (im: Img) => asyncImg.checkInImgData(im.getSubImage(xOffset, yOffset, w, h)) )

    asyncImg
  }

  def setSubImage(xOffset: Int, yOffset: Int, sourceImage: Img, sxOffset: Int, syOffset: Int, w: Int, h: Int):  Future[AsyncImg] = reserveImgData[AsyncImg] (
    (im: Img) => {
      im.setSubImage(xOffset, yOffset, sourceImage, sxOffset, syOffset, w, h)
      this
    }
  )

  def snapshot: Future[Img] = reserveImgData[Img] (
    (im: Img) => im.copy()
  )

  def copy(): AsyncImg = getSubImage(0, 0, width, height)

  override def toString: String = s"ImgAsync($width X $height)"

}

class LostPixelDataException extends Exception("Could not procure Pixel Data.")
