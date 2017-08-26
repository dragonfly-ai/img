package ai.dragonfly.img.async

/**
 * Created by clifton on 5/2/17.
 */

import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer, TypedArrayBufferOps}

@js.native @js.annotation.JSGlobalScope
object WorkerGlobal extends js.Any {
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any): Unit = js.native
}

@JSExport("ImgWorker")
object ImgWorker {
  @JSExport
  def main(): Unit = {
    WorkerGlobal.addEventListener("message", onMessage _ )
    WorkerGlobal.postMessage(s"Started")
  }

  def onMessage(msg: dom.MessageEvent) = {
    val invocationMsg: ImgOpsInvocationMsg = ImgOpsInvocationMsg.fromBytes(TypedArrayBuffer.wrap(msg.data.asInstanceOf[ArrayBuffer]))
    println("received message: " + invocationMsg)
    val responsePayload: ArrayBuffer = new TypedArrayBufferOps(invocationMsg.execute().toBytes).arrayBuffer()
    WorkerGlobal.postMessage(responsePayload)
  }
}
