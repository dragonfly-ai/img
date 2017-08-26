package ai.dragonfly.img.async

import java.nio.ByteBuffer
import java.util
import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.img.Img
import org.scalajs.dom
import org.scalajs.dom.raw.{Transferable, Worker}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by clifton on 5/30/17.
 */


@JSExport("ImgOpsClient")
object ImgOpsClient {


  // lazy because the user may not want to use asynchronous image processing.

  private lazy val imgOpsRegistry = new util.HashMap[Long, Promise[ImgOpsResponseMsg]]()

  private lazy val imgWorker = new Worker("ImgWorker.js")

  imgWorker.addEventListener(
    "message",
    ( msg: dom.MessageEvent ) => {
      println("received message: " + msg.data)
      msg.data match {
        case s: String => println(s)
        case ab: ArrayBuffer =>
          val wrapped: ByteBuffer = TypedArrayBuffer.wrap(ab)
          try {
            val responseMsg = ImgOpsResponseMsg.fromBytes(wrapped)
            println(responseMsg)
            val promise = imgOpsRegistry.get(responseMsg.id)
            promise.success(responseMsg)
          } catch {
            case e: Throwable => println(e.getStackTrace)
          }

        case _ => println("Recieved unknown message type.")
      }

    }
  )

  def apply(msg: ImgOpsInvocationMsg): Future[ImgOpsResponseMsg] = {
    val promise = Promise[ImgOpsResponseMsg]
    imgOpsRegistry.put(msg.id, promise)
    val msgPayload: ArrayBuffer = new TypedArrayBufferOps(msg.toBytes).arrayBuffer()
//    val transferables: js.UndefOr[js.Array[Transferable]] = Some(js.Array[Transferable](msgPayload)).orUndefined
    val transferables: js.Array[Transferable] = js.Array[Transferable](msgPayload)

    imgWorker.postMessage(msgPayload, transferables)
    promise.future
  }

  def next:Long = ({
      val atom: AtomicLong = new AtomicLong(0L)
      () => atom.incrementAndGet()
    })()

  // operations:
  def randomizeRGB(img: Img): Future[ImgOpsResponseMsg] = {
    ImgOpsClient(MsgInvokeRandomRGB(next, img.width, img.height))
  }

  @JSExport def randomizeRGB(img: Img, callback: js.Function1[Img, Any]): Unit = {

    randomizeRGB(img) onComplete {
      case scala.util.Success(response: ImgResponseMsg) =>
        println("response.img dimensions: " + response.img.width + " " + response.img.height)
        callback(response.img)
      case scala.util.Success(_) => println("Unexpected response: " + _)
      case Failure(t) => println("failed")
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