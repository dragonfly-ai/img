package ai.dragonfly.img.async

import java.nio.ByteBuffer

import ai.dragonfly.color._
import ai.dragonfly.distributed.Snowflake
import ai.dragonfly.img.{ImageBasics, Img, ImgOps}
import boopickle.Default._

import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8ClampedArray}

/**
 * Created by clifton on 6/5/17.
 */

object ImgOpsTransferable {

  // pickle Image Operations Messages.
  implicit val imgOpsPickler = compositePickler[ImgOpsTransferable].
    addConcreteType[ImgMsg].
    addConcreteType[PaletteMsg].
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

  // pickle color types
  implicit val labPickler = compositePickler[LAB].
    addConcreteType[FastFatLab].
    addConcreteType[SlowSlimLab]

  implicit val luvPickler = compositePickler[LUV].
    addConcreteType[FastFatLuv].
    addConcreteType[SlowSlimLuv]

  implicit val xyzPickler = compositePickler[XYZ].
    addConcreteType[FastFatXYZ].
    addConcreteType[SlowSlimXYZ]

  implicit val colorPickler = compositePickler[Color].
    addConcreteType[RGBA].
    addConcreteType[HSV].
    addConcreteType[HSL].
    addConcreteType[CMYK].
    addConcreteType[XYZ].
    addConcreteType[LAB].
    addConcreteType[LUV]


  implicit def toByteBuffer(ab: ArrayBuffer): ByteBuffer = scala.scalajs.js.typedarray.TypedArrayBuffer.wrap(ab)

  implicit def toArrayBuffer(bb: ByteBuffer): ArrayBuffer = {
    scala.scalajs.js.typedarray.TypedArrayBufferOps.byteBufferOps(bb).arrayBuffer()
  }

  def jsArrToImg(width: Int, jsArr: js.Array[_], index: Int = 1): Img = {
    new Img(
      width,
      new Uint8ClampedArray(jsArr(index).asInstanceOf[ArrayBuffer])
    )
  }

  def jsArrToColorPalette(jsArr: js.Array[_], index: Int = 1): ColorPalette = {
    new ColorPalette(Unpickle[Array[ColorFrequency]].fromBytes(jsArr(index).asInstanceOf[ArrayBuffer]))
  }

  def colorPaletteToBytes(cp: ColorPalette): ArrayBuffer = {
    Pickle.intoBytes(cp.colorFrequencies)
  }

}

trait PromiseWrapper {
  def p: Promise[_]
}

case class ImgPromise(p: Promise[Img]) extends PromiseWrapper
case class PalettePromise(p: Promise[ColorPalette]) extends PromiseWrapper

// for composite pickler
sealed trait ImgOpsTransferable {
  def id: Long
}

trait ImgOpsMsg[T] extends ImgOpsTransferable {
  def apply(parameters: js.Array[_]): T
  def promiseWrapper: PromiseWrapper
}

trait YieldsImg extends ImgOpsMsg[Img] {
  override def promiseWrapper: ImgPromise = ImgPromise(Promise[Img])
}

trait YieldsColorPalette extends ImgOpsMsg[ColorPalette] {
  override def promiseWrapper: PalettePromise = PalettePromise(Promise[ColorPalette])
}

case class ImgMsg(override val id: Long, width: Int) extends YieldsImg {
  override def apply(parameters: js.Array[_]): Img = ImgOpsTransferable.jsArrToImg(width, parameters)
}

case class PaletteMsg(override val id: Long) extends YieldsColorPalette {
  override def apply(parameters: js.Array[_]): ColorPalette = ImgOpsTransferable.jsArrToColorPalette(parameters)
}

// ImgOps message classes:

case class RandomRgbMsg(override val id: Long, width: Int, height: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = ImgOps.randomizeRGB(new Img(width, height)).asInstanceOf[Img]
}

case class FlipHorizontalMsg(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.flipHorizontal(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class FlipVerticalMsg(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.flipVertical(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class Rotate90DegreesMsg(override val id: Long, width: Int, counterClockwise: Boolean) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.rotate90Degrees(ImgOpsTransferable.jsArrToImg(width, parameters), counterClockwise).asInstanceOf[Img]
  }
}

case class Rotate180DegreesMsg(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.rotate180Degrees(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class RotateDegreesMsg(override val id: Long, width: Int, angle: Double) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.rotateDegrees(ImgOpsTransferable.jsArrToImg(width, parameters), angle).asInstanceOf[Img]
  }
}

case class RotateRadiansMsg(override val id: Long, width: Int, angleRadians: Double) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.rotateRadians(ImgOpsTransferable.jsArrToImg(width, parameters), angleRadians).asInstanceOf[Img]
  }
}

case class OverlayMsg(
  override val id: Long,
  bgImgWidth: Int, fgImgWidth: Int,
  bgX: Int, bgY: Int, fgX: Int, fgY: Int,
  width: Int, height: Int ) extends  YieldsImg {

  override def apply(parameters: js.Array[_]): Img = {
    println(this)
    println(parameters)
    ImgOps.overlay(
      ImgOpsTransferable.jsArrToImg(bgImgWidth, parameters, 1),
      ImgOpsTransferable.jsArrToImg(fgImgWidth, parameters, 2),
      bgX, bgY, fgX, fgY, width, height).asInstanceOf[Img]
  }
}

// blur:
case class EpanechnikovBlurRGBMsg(override val id: Long, width: Int, radius: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.epanechnikovBlurRGB(ImgOpsTransferable.jsArrToImg(width, parameters), radius).asInstanceOf[Img]
  }
}

case class UniformBlurRGBMsg(override val id: Long, width: Int, radius: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.uniformBlurRGB(ImgOpsTransferable.jsArrToImg(width, parameters), radius).asInstanceOf[Img]
  }
}

case class GaussianBlurRGBMsg(override val id: Long, width: Int, radius: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.gaussianBlurRGB(ImgOpsTransferable.jsArrToImg(width, parameters), radius).asInstanceOf[Img]
  }
}


case class UnsharpenMaskRGBMsg(override val id: Long, width: Int, radius: Int, amount: Double, threshold: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.unsharpenMaskRGB(ImgOpsTransferable.jsArrToImg(width, parameters), radius, amount, threshold).asInstanceOf[Img]
  }
}

case class UnsharpenMaskLABMsg(override val id: Long, width: Int, radius: Int, amount: Double, threshold: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.unsharpenMaskLAB(ImgOpsTransferable.jsArrToImg(width, parameters), radius, amount, threshold).asInstanceOf[Img]
  }
}


case class DifferenceMatteMsg(override val id: Long, width1: Int, width2: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    val img1 = new Img(
      width1,
      new Uint8ClampedArray(parameters(1).asInstanceOf[ArrayBuffer])
    )
    val img2 = new Img(
      width2,
      new Uint8ClampedArray(parameters(2).asInstanceOf[ArrayBuffer])
    )
    ImgOps.differenceMatte(
      ImgOpsTransferable.jsArrToImg(width1, parameters, 1),
      ImgOpsTransferable.jsArrToImg(width2, parameters, 2)
    ).asInstanceOf[Img]
  }
}

case class ScaleMsg(override val id: Long, width: Int, newWidth: Int, newHeight: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.scale(ImgOpsTransferable.jsArrToImg(width, parameters), newWidth, newHeight).asInstanceOf[Img]
  }
}

case class GrayscaleAverageRGBMSG(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.grayscaleAverageRGB(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class GrayscaleLABIntensityMSG(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.grayscaleLABIntensity(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class EqualizeRGBMSG(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.equalizeRGB(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class NegativeMSG(override val id: Long, width: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.negative(ImgOpsTransferable.jsArrToImg(width, parameters)).asInstanceOf[Img]
  }
}

case class ThresholdLabMsg(override val id: Long, width: Int, intensity: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.thresholdLab(ImgOpsTransferable.jsArrToImg(width, parameters), intensity).asInstanceOf[Img]
  }
}

case class ThresholdRGBMsg(override val id: Long, width: Int, intensity: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.thresholdRGB(ImgOpsTransferable.jsArrToImg(width, parameters), intensity).asInstanceOf[Img]
  }
}

case class BrightnessMsg(override val id: Long, width: Int, brightness: Int) extends  YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.brightness(ImgOpsTransferable.jsArrToImg(width, parameters), brightness).asInstanceOf[Img]
  }
}

case class ContrastMsg(override val id: Long, width: Int, contrast: Double) extends YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.contrast(ImgOpsTransferable.jsArrToImg(width, parameters), contrast).asInstanceOf[Img]
  }
}

case class MedianMsg(override val id: Long, width: Int, radius: Int) extends YieldsImg {
  override def apply(parameters: js.Array[_]): Img = {
    ImgOps.median(ImgOpsTransferable.jsArrToImg(width, parameters), radius).asInstanceOf[Img]
  }
}

case class ConcisePaletteMsg(override val id: Long, width: Int) extends YieldsColorPalette {
  override def apply(parameters: js.Array[_]): ColorPalette = {
    ImgOps.concisePalette(ImgOpsTransferable.jsArrToImg(width, parameters))
  }
}