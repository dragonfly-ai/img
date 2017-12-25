package ai.dragonfly.img.async

import java.nio.ByteBuffer

import ai.dragonfly.distributed.Snowflake
import ai.dragonfly.img.{ImageBasics, ImgOps, Img}
import boopickle.Default._

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8ClampedArray}

/**
 * Created by clifton on 6/5/17.
 */

object ImgOpsMsg {
  implicit val imgOpsPickler = compositePickler[ImgOpsMsg].
    addConcreteType[ImgMsg].
    addConcreteType[RandomRgbMsg].
    addConcreteType[EpanechnikovBlurRGB].
    addConcreteType[UniformBlurRGB].
    addConcreteType[GaussianBlurRGB].
    addConcreteType[Overlay]

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

// ImgOps message classes:

case class RandomRgbMsg(override val id: Long, width: Int, height: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = ImgOps.randomizeRGB(new Img(width, height)).asInstanceOf[Img]
}

case class Overlay(
  override val id: Long,
  bgImgWidth: Int, fgImgWidth: Int,
  bgX: Int, bgY: Int, fgX: Int, fgY: Int,
  width: Int, height: Int ) extends ImgOpsMsg {

  override def apply(parameters: js.Array[_]): Img = {
    val bgImg = new Img(
      bgImgWidth,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    val fgImg = new Img(
      fgImgWidth,
      new Uint8ClampedArray(parameters(2).asInstanceOf[ArrayBuffer])
    )
    ImgOps.overlay(bgImg, fgImg, bgX, bgY, fgX, fgY, width, height).asInstanceOf[Img]
  }
}

// blur:
case class EpanechnikovBlurRGB(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.epanechnikovBlurRGB(img, radius).asInstanceOf[Img]
  }
}

case class UniformBlurRGB(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.uniformBlurRGB(img, radius).asInstanceOf[Img]
  }
}

case class GaussianBlurRGB(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.gaussianBlurRGB(img, radius).asInstanceOf[Img]
  }
}