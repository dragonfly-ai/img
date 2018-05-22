package ai.dragonfly.img.async

/**
 * Created by clifton on 5/2/17.
 */

import java.nio.ByteBuffer

import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.ArrayBuffer

import boopickle.Default._

import ImgOpsTransferable._

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
    //println("Worker received message: " + msg.data)
    msg.data match {
      case s: String => println("worker received string: " + s)
      case jsArr: js.Array[Transferable] =>

        val wrapped: ByteBuffer = jsArr(0).asInstanceOf[ArrayBuffer]

        val iot = Unpickle[ImgOpsTransferable].fromBytes(wrapped.asInstanceOf[ByteBuffer])
        iot match {
          case transferred: Boomerang =>
            println(s"worker received: $transferred")
            val response = transferred(jsArr)
            //println(s"response: $response")

            if (response.length > 0) {
              val responseTransferable = js.Array[Transferable]()
              for (t <- response) responseTransferable.push(t)
              WorkerGlobal.postMessage(response, responseTransferable) // return the results.
            }


            WorkerGlobal.postMessage(jsArr, jsArr) // return the invocation parameters.

          case _ => WorkerGlobal.postMessage(s"Worker received non-Boomerang message: $iot")
        }

      case _ => WorkerGlobal.postMessage(s"Worker received unknown message type: ${msg.data}")
    }
  }
}
