package ai.dragonfly.img.async

import java.nio.ByteBuffer
import java.util
import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.distributed.Snowflake
import ai.dragonfly.img.{ImageBasics, Img}
import org.scalajs.dom
import org.scalajs.dom.raw.{Transferable, Worker}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import boopickle.Default._
import ai.dragonfly.img.async.ImgOpsMsg._

/**
 * Created by clifton on 5/30/17.
 */


@JSExport("ImgOpsClient")
object ImgOpsClient {


  // lazy because the user may not want to use asynchronous image processing.

  private lazy val imgOpsRegistry = new util.HashMap[Long, Promise[Img]]()

  private lazy val imgWorker = new Worker("ImgWorker.js")

  imgWorker.addEventListener( // handle messages from the worker
    "message",
    (msg: dom.MessageEvent) => {
      println("Client received msg.data: " + msg.data)
      msg.data match {
        case s: String => println(s)
        case jsArr: js.Array[_] =>
          val bytes: ByteBuffer = jsArr(0).asInstanceOf[ArrayBuffer]
          val responseMsg: ImgOpsMsg = Unpickle[ImgOpsMsg].fromBytes(bytes)
          println(s"Client received message: $responseMsg")
          val imgResult: Img = responseMsg(jsArr)
          val promise = imgOpsRegistry.get(responseMsg.id)
          promise.success(imgResult)

        case _ => println("Client received unknown message type.")
      }

    }
  )

  def apply(msg: ImgOpsMsg, transferList: js.Array[Transferable] = js.Array[Transferable]()): Future[Img] = {
    val promise = Promise[Img]
    imgOpsRegistry.put(msg.id, promise)
    println(msg)
    val bytes: ArrayBuffer = Pickle.intoBytes(msg)
    val msgPayload = js.Array[Any](bytes)

    for (t <- transferList) msgPayload.push(t)
    imgWorker.postMessage(msgPayload, transferList)
    println(msgPayload)
    promise.future
  }

  // operations:
  def randomizeRGB(img: Img): Future[Img] = ImgOpsClient(RandomRgbMsg(Snowflake(), img.width, img.height))
  @JSExport def randomizeRGB(img: Img, callback: js.Function1[Img, Any]): Unit = jsCallbackHandler(randomizeRGB(img), callback)

  def overlay(
    bgImg: Img, fgImg: Img,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int
  ): Future[Img] = {
    ImgOpsClient(
      OverlayMsg(Snowflake(), bgImg.width, fgImg.width, bgX, bgY, fgX, fgY, width, height),
      js.Array[Transferable](bgImg.pixelData.buffer, fgImg.pixelData.buffer)
    )
  }
  @JSExport def overlay(
    bgImg: Img, fgImg: Img,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int,
    callback: js.Function1[Img, Any]
  ): Unit = jsCallbackHandler(overlay(bgImg, fgImg, bgX, bgY, fgX, fgY, width, height), callback)

  def epanechnikovBlurRGB(img: Img, radius: Int): Future[Img] = ImgOpsClient(EpanechnikovBlurRGBMsg(Snowflake(), img.width, radius), js.Array[Transferable](img.pixelData.buffer))
  @JSExport def epanechnikovBlurRGB(img: Img, radius: Int, callback: js.Function1[Img, Any]): Unit = jsCallbackHandler(epanechnikovBlurRGB(img, radius), callback)

  def uniformBlurRGB(img: Img, radius: Int): Future[Img] = ImgOpsClient(UniformBlurRGBMsg(Snowflake(), img.width, radius), js.Array[Transferable](img.pixelData.buffer))
  @JSExport def uniformBlurRGB(img: Img, radius: Int, callback: js.Function1[Img, Any]): Unit = jsCallbackHandler(uniformBlurRGB(img, radius), callback)

  def gaussianBlurRGB(img: Img, radius: Int): Future[Img] = ImgOpsClient(GaussianBlurRGBMsg(Snowflake(), img.width, radius), js.Array[Transferable](img.pixelData.buffer))
  @JSExport def gaussianBlurRGB(img: Img, radius: Int, callback: js.Function1[Img, Any]): Unit = jsCallbackHandler(gaussianBlurRGB(img, radius), callback)


  def differenceMatte(img1: Img, img2: Img): Future[Img] = ImgOpsClient(DifferenceMatteMsg(Snowflake(), img1.width, img2.width), js.Array[Transferable](img1.pixelData.buffer, img2.pixelData.buffer))
  @JSExport def differenceMatte(img1: Img, img2: Img, callback: js.Function1[Img, Any]): Unit = jsCallbackHandler(differenceMatte(img1, img2), callback)

  def scale(img: Img, newWidth: Int, newHeight: Int): Future[Img] = ImgOpsClient(ScaleMsg(Snowflake(), img.width, newWidth, newHeight), js.Array[Transferable](img.pixelData.buffer))
  @JSExport def scale(img1: Img, newWidth: Int, newHeight: Int, callback: js.Function1[Img, Any]): Unit = jsCallbackHandler(scale(img1, newWidth, newHeight), callback)


  // handle asynch image processing operations from javascript.
  def jsCallbackHandler(imgFuture: Future[Img], callback: js.Function1[Img, Any]): Unit = {
    imgFuture onComplete {
      case scala.util.Success(img: Img) =>
        println("response.img dimensions: " + img.width + " " + img.height)
        callback(img)
      case scala.util.Success(_) => println("Unexpected response: " + _)
      case Failure(t) => println("failed" + t)
    }

  }

}