package ai.dragonfly.img.async

import java.nio.ByteBuffer
import java.util
import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.distributed.Snowflake
import ai.dragonfly.img.Img
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

  imgWorker.addEventListener(  // handle messages from the worker
    "message",
    ( msg: dom.MessageEvent ) => {
      println("Client received message: " + msg.data)
      msg.data match {
        case s: String => println(s)
        case jsArr: js.Array[_] =>
          val wrapped: ByteBuffer = jsArr(0).asInstanceOf[ArrayBuffer]
          println(s"Client wrapped: ${wrapped.toString}")
          val responseMsg: ImgOpsMsg = Unpickle[ImgOpsMsg].fromBytes(wrapped)
          println(s"Client received message: $responseMsg")
          val imgResult: Img = responseMsg(jsArr)
          val promise = imgOpsRegistry.get(responseMsg.id)
          promise.success( imgResult )

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
    println(s"Client msgPayLoad: $msgPayload")

    for (t <- transferList) msgPayload.push(t)
    imgWorker.postMessage(msgPayload, transferList)
    println(msgPayload)
    promise.future
  }

  // operations:
  def randomizeRGB(img: Img): Future[Img] = {
    val msg = RandomRgbMsg(Snowflake(), img.width, img.height)
    println(msg)
    ImgOpsClient(msg)
  }

  @JSExport def randomizeRGB(img: Img, callback: js.Function1[Img, Any]): Unit = {

    randomizeRGB(img) onComplete {
      case scala.util.Success(img: Img) =>
        println("response.img dimensions: " + img.width + " " + img.height)
        callback(img)
      case scala.util.Success(_) => println("Unexpected response: " + _)
      case Failure(t) => println("failed" + t)
    }

  }
  //
//  @JSExport def overlay(
//    bgImg: ImageBasics,
//    fgImg: ImageBasics,
//    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
//    width: Int, height: Int
//  ): Future[ImageBasics] = {
//
//  }
//
//  @JSExport def epanechnikovBlurRGB(toBlur: ImageBasics, radius: Int): Future[ImageBasics] = {
//
//  }
//
//  @JSExport def uniformBlurRGB(toBlur: ImageBasics, radius: Int): Future[ImageBasics] = {
//
//  }
//
//  @JSExport def gaussianBlurRGB(toBlur: ImageBasics, radius: Int): Future[ImageBasics] = {
//
//  }

}


/*
def sendRequest[R](msg:Message):Future[R] = {
  val promise = Promise[R]
  val id = nextRequestId()
  val envelope = Envelope(id, msg)
  register(id, promise)
  sendToWorker(envelope)
  promise.future
}

The worker processes msg, wraps the result in another Envelope, and the result gets handled back in the main thread with something like:

def handleResult(resultEnv:Envelope):Unit = {
  val promise = findRegistered(resultEnv.id)
  val result = resultEnv.msg
  promise.success(result)
}
 */