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
    addConcreteType[FlipHorizontalMsg].
    addConcreteType[FlipVerticalMsg].
    addConcreteType[Rotate90DegreesMsg].
    addConcreteType[Rotate180DegreesMsg].
    addConcreteType[RotateDegreesMsg].
    addConcreteType[RotateRadiansMsg].
    addConcreteType[EpanechnikovBlurRGBMsg].
    addConcreteType[UniformBlurRGBMsg].
    addConcreteType[GaussianBlurRGBMsg].
    addConcreteType[UnsharpenMaskRGBMsg].
    addConcreteType[UnsharpenMaskLABMsg].
    addConcreteType[MedianMsg].
    addConcreteType[OverlayMsg].
    addConcreteType[DifferenceMatteMsg].
    addConcreteType[GrayscaleAverageRGBMSG].
    addConcreteType[GrayscaleLABIntensityMSG].
    addConcreteType[NegativeMSG].
    addConcreteType[ThresholdLabMsg].
    addConcreteType[ThresholdRGBMsg].
    addConcreteType[BrightnessMsg].
    addConcreteType[ContrastMsg].
    addConcreteType[EqualizeRGBMSG].
    addConcreteType[ScaleMsg].
    addConcreteType[ConcisePaletteMsg]

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

case class FlipHorizontalMsg(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.flipHorizontal(img).asInstanceOf[Img]
  }
}

case class FlipVerticalMsg(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.flipVertical(img).asInstanceOf[Img]
  }
}

case class Rotate90DegreesMsg(override val id: Long, width: Int, counterClockwise: Boolean) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.rotate90Degrees(img, counterClockwise).asInstanceOf[Img]
  }
}

case class Rotate180DegreesMsg(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.rotate180Degrees(img).asInstanceOf[Img]
  }
}

case class RotateDegreesMsg(override val id: Long, width: Int, angle: Double) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.rotateDegrees(img, angle).asInstanceOf[Img]
  }
}

case class RotateRadiansMsg(override val id: Long, width: Int, angleRadians: Double) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.rotateRadians(img, angleRadians).asInstanceOf[Img]
  }
}

case class OverlayMsg(
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
case class EpanechnikovBlurRGBMsg(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.epanechnikovBlurRGB(img, radius).asInstanceOf[Img]
  }
}

case class UniformBlurRGBMsg(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.uniformBlurRGB(img, radius).asInstanceOf[Img]
  }
}

case class GaussianBlurRGBMsg(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.gaussianBlurRGB(img, radius).asInstanceOf[Img]
  }
}


case class UnsharpenMaskRGBMsg(override val id: Long, width: Int, radius: Int, amount: Double, threshold: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.unsharpenMaskRGB(img, radius, amount, threshold).asInstanceOf[Img]
  }
}

case class UnsharpenMaskLABMsg(override val id: Long, width: Int, radius: Int, amount: Double, threshold: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.unsharpenMaskLAB(img, radius, amount, threshold).asInstanceOf[Img]
  }
}


case class DifferenceMatteMsg(override val id: Long, width1: Int, width2: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img1 = new Img(
      width1,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    val img2 = new Img(
      width2,
      new Uint8ClampedArray(parameters(2).asInstanceOf[ArrayBuffer])
    )
    ImgOps.differenceMatte(img1, img2).asInstanceOf[Img]
  }
}

case class ScaleMsg(override val id: Long, width: Int, newWidth: Int, newHeight: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.scale(img, newWidth, newHeight).asInstanceOf[Img]
  }
}

case class GrayscaleAverageRGBMSG(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.grayscaleAverageRGB(img).asInstanceOf[Img]
  }
}

case class GrayscaleLABIntensityMSG(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.grayscaleLABIntensity(img).asInstanceOf[Img]
  }
}

case class EqualizeRGBMSG(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.equalizeRGB(img).asInstanceOf[Img]
  }
}

case class NegativeMSG(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.negative(img).asInstanceOf[Img]
  }
}

case class ThresholdLabMsg(override val id: Long, width: Int, intensity: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.thresholdLab(img, intensity).asInstanceOf[Img]
  }
}

case class ThresholdRGBMsg(override val id: Long, width: Int, intensity: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.thresholdRGB(img, intensity).asInstanceOf[Img]
  }
}

case class BrightnessMsg(override val id: Long, width: Int, brightness: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.brightness(img, brightness).asInstanceOf[Img]
  }
}

case class ContrastMsg(override val id: Long, width: Int, contrast: Double) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.contrast(img, contrast).asInstanceOf[Img]
  }
}

case class MedianMsg(override val id: Long, width: Int, radius: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.median(img, radius).asInstanceOf[Img]
  }
}

case class ConcisePaletteMsg(override val id: Long, width: Int) extends ImgOpsMsg {
  override def apply(parameters: js.Array[_]): Img = {
    val img = new Img(
      width,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    ImgOps.concisePalette(img).asInstanceOf[Img]
  }
}