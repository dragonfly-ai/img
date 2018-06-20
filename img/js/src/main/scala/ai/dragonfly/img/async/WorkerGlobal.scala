package ai.dragonfly.img.async

/**
  * Created by clifton on 5/2/17.
  */

import org.scalajs.dom.raw._

import scala.scalajs.js

@js.native @js.annotation.JSGlobalScope
object WorkerGlobal extends js.Any {
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any, transferList: js.Array[Transferable] = js.Array[Transferable]()): Unit = js.native
}