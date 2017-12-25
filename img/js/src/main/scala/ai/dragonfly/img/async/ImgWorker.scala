package ai.dragonfly.img.async

/**
 * Created by clifton on 5/2/17.
 */

import java.nio.ByteBuffer

import ai.dragonfly.img.Img
import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer, TypedArrayBufferOps}
import boopickle.Default._
import ImgOpsMsg._

@js.native @js.annotation.JSGlobalScope
object WorkerGlobal extends js.Any {
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any, transferList: js.Array[Transferable] = js.Array[Transferable]()): Unit = js.native
}

@JSExport("ImgWorker")
object ImgWorker {
  @JSExport
  def main(): Unit = {
    WorkerGlobal.addEventListener("message", onMessage _ )
    WorkerGlobal.postMessage(s"Started")
  }

  def onMessage(msg: dom.MessageEvent) = {
    println("Worker received message: " + msg.data)
    msg.data match {
      case s: String => println("worker received string: " + s)
      case jsArr: js.Array[_] =>
        val wrapped: ByteBuffer = jsArr(0).asInstanceOf[ArrayBuffer]
        println(s"Worker wrapped: ${wrapped.toString}")
        println(s"Worker received jsArray: $wrapped")
        // val invocationMsg: ImgOpsMsg = Unpickle[ImgOpsMsg].fromBytes(TypedArrayBuffer.wrap(jsArr(0).asInstanceOf[ArrayBuffer]))
        val invocationMsg: ImgOpsMsg = Unpickle[ImgOpsMsg].fromBytes(wrapped.asInstanceOf[ByteBuffer])
        println(s"Worker received invocationMsg: $invocationMsg")
        val imgResult: Img = invocationMsg(jsArr)
        //val responsePayload: ArrayBuffer = new TypedArrayBufferOps(invocationMsg.execute().toBytes).arrayBuffer()
        val imgBytes = imgResult.pixelData.buffer //scala.scalajs.js.typedarray.TypedArrayBufferOps.byteBufferOps(imgResult.pixelData).arrayBuffer()
        val imgMsg: ImgOpsMsg = ImgMsg(invocationMsg.id, imgResult.width)
        println(s"Worker imgMsg: $imgMsg")
        val pickledBytes = Pickle.intoBytes( imgMsg )
        println(s"Worker pickledBytes: $pickledBytes")
        val imgMsgBytes: ArrayBuffer = pickledBytes
        println(s"Worker imgMsgBytes: $imgMsgBytes")
        WorkerGlobal.postMessage(
          js.Array(imgMsgBytes, imgBytes),
          js.Array(imgMsgBytes, imgBytes)
        )
      case _ => println("Worker received unknown message type.")
    }
  }
}
