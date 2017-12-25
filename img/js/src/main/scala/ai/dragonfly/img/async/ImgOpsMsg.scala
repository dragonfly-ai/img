package ai.dragonfly.img.async

import java.nio.ByteBuffer

import ai.dragonfly.distributed.Snowflake
import ai.dragonfly.img.{ImageBasics, ImageOperations, Img}
import boopickle.Default._

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8ClampedArray}

/**
 * Created by clifton on 6/5/17.
 */

object ImgOpsMsg {
  implicit val imgOpsPickler = compositePickler[ImgOpsMsg].
    addConcreteType[ImgMsg].
    addConcreteType[RandomRgbMsg]

  implicit def toByteBuffer(ab: ArrayBuffer): ByteBuffer = scala.scalajs.js.typedarray.TypedArrayBuffer.wrap(ab)

  implicit def toArrayBuffer(bb: ByteBuffer): ArrayBuffer = {
    scala.scalajs.js.typedarray.TypedArrayBufferOps.byteBufferOps(bb).arrayBuffer()
  }
}

sealed trait ImgOpsMsg {
  def id: Long
  def apply(parameters: js.Array[_]): Img
}

case class ImgMsg(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = new Img(
    width,
    new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
  )
}

case class RandomRgbMsg(override val id: Long, width: Int, height: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = ImageOperations.randomizeRGB(new Img(width, height)).asInstanceOf[Img]

}


/*

  6 + 2 = 8  // 12 - 2 = 10

  This means that bed time is: 10:00 PM and every other day is code day from 6:00 PM until 10:00 PM.
  That is 4 hours of code time every other day.

  Uri time, Sharvari time.

  What am I afraid of?
  Vinita

  What should it look like?

    Transferable array.

 */

