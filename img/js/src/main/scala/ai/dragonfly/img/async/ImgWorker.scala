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
import ImgOpsTransferable._
import ai.dragonfly.color.ColorPalette

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
        // val invocationMsg: ImgOpsMsg = Unpickle[ImgOpsMsg].fromBytes(TypedArrayBuffer.wrap(jsArr(0).asInstanceOf[ArrayBuffer]))
        val transferred: ImgOpsTransferable = Unpickle[ImgOpsTransferable].fromBytes(wrapped.asInstanceOf[ByteBuffer])
        println(s"Worker received message: $transferred")
        transferred match {
          case imgInvocationMsg: YieldsImg =>
            val imgResult: Img = imgInvocationMsg(jsArr)
            val imgBytes = imgResult.pixelData.buffer
            val imgMsg: ImgOpsTransferable = ImgMsg(imgInvocationMsg.id, imgResult.width)
            println(s"Worker response message: $imgMsg")
            val imgMsgBytes: ArrayBuffer = Pickle.intoBytes( imgMsg )
            WorkerGlobal.postMessage(
              js.Array(imgMsgBytes, imgBytes),
              js.Array(imgMsgBytes, imgBytes)
            )
          case paletteInvocationMsg: YieldsColorPalette =>
            val paletteResult: ColorPalette = paletteInvocationMsg(jsArr)
            val paletteBytes = colorPaletteToBytes(paletteResult)
            val paletteMsg: ImgOpsTransferable = PaletteMsg(paletteInvocationMsg.id)
            println(s"Worker response message: $paletteMsg")
            val paletteMsgBytes: ArrayBuffer = Pickle.intoBytes( paletteMsg )
            WorkerGlobal.postMessage(
              //js.Array(paletteMsgBytes, paletteBytes),
              js.Array(paletteMsgBytes, paletteBytes)
            )
        }

      case _ => println("Worker received unknown message type.")
    }
  }
}
