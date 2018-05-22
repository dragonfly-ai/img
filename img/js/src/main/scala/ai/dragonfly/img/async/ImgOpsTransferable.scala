package ai.dragonfly.img.async

import java.nio.ByteBuffer

import ai.dragonfly.color._
import ai.dragonfly.img.{Img, ImgAsync, ImgOps}
import boopickle.Default.{Pickle, _}
import com.sun.net.httpserver.Authenticator.Success
import org.scalajs.dom.raw.Transferable

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8ClampedArray}
import scala.util.{Failure, Success}

/**
 * Created by clifton on 6/5/17.
 */

object ImgOpsTransferable {

  // pickle Image Operations Messages.
  implicit val imgOpsPickler = compositePickler[ImgOpsTransferable].
    addConcreteType[ImgMsg].
    addConcreteType[PaletteMsg].
    addConcreteType[FlipHorizontalMsg].
    addConcreteType[RandomRgbMsg].
    addConcreteType[RandomLabMsg].
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
    addConcreteType[ConcisePaletteMsg].
    addConcreteType[ProjectToPaletteMsg]

  // pickle color types
  implicit val colorPickler = compositePickler[Color].
    addConcreteType[RGBA].
    addConcreteType[HSV].
    addConcreteType[HSL].
    addConcreteType[CMYK].
    addConcreteType[FastFatXYZ].
    addConcreteType[SlowSlimXYZ].
    addConcreteType[FastFatLab].
    addConcreteType[SlowSlimLab].
    addConcreteType[FastFatLuv].
    addConcreteType[SlowSlimLuv]


  implicit def toByteBuffer(ab: ArrayBuffer): ByteBuffer = scala.scalajs.js.typedarray.TypedArrayBuffer.wrap(ab)

  implicit def toArrayBuffer(bb: ByteBuffer): ArrayBuffer = {
    scala.scalajs.js.typedarray.TypedArrayBufferOps.byteBufferOps(bb).arrayBuffer()
  }

  implicit def toTransferrable(cp: ColorPalette): Transferable = colorPaletteToBytes(cp)

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
  def apply(msg: ImgOpsTransferable, jsArr: js.Array[Transferable]): Unit
}

case class ImgPromise(p: Promise[Img]) extends PromiseWrapper {
  override def apply(msg: ImgOpsTransferable, jsArr: js.Array[Transferable]): Unit = {
    val imgResult: Img = msg.asInstanceOf[ImgMsg](jsArr)
    p.success(imgResult)
  }
}
case class PalettePromise(p: Promise[ColorPalette]) extends PromiseWrapper {
  override def apply(msg: ImgOpsTransferable, jsArr: js.Array[Transferable]): Unit = {
    val paletteResult: ColorPalette = msg.asInstanceOf[PaletteMsg](jsArr)
    p.success(paletteResult)
  }
}

case class ParamPromise(p: Promise[js.Array[Transferable]]) extends PromiseWrapper {
  override def apply(msg: ImgOpsTransferable, jsArr: js.Array[Transferable]): Unit = {
    p.success(jsArr)
  }
}

// for composite pickler
sealed trait ImgOpsTransferable {
  def resultId: Long
}

// used only to send results back to the main thread.
trait Dart[T] {
  def apply(jsArr: js.Array[Transferable]): T
}

// used only to send data and method invocations to the worker thread.
trait Boomerang extends ImgOpsTransferable {
  def paramRecoveryId: Long
  def promiseWrapper: PromiseWrapper
  def apply(parameters: js.Array[Transferable]): js.Array[Transferable]
  def bundle(img: Img): js.Array[Transferable] = {
    val responseMsg = ImgMsg(resultId, img.width)

    js.Array[Transferable](
      ImgOpsTransferable.toArrayBuffer(Pickle.intoBytes(responseMsg.asInstanceOf[ImgOpsTransferable])),
      img.pixelData.buffer
    )
  }
}


trait YieldsImg extends Boomerang {
  override def promiseWrapper: ImgPromise = ImgPromise(Promise[Img])
}


trait MutatesImg extends Boomerang {
  override def promiseWrapper: ParamPromise = ParamPromise(Promise[js.Array[Transferable]])
  override def paramRecoveryId: Long = resultId
}

trait YieldsColorPalette extends Boomerang {
  override def promiseWrapper: PalettePromise = PalettePromise(Promise[ColorPalette])
}

case class ImgMsg(override val resultId: Long, width: Int) extends ImgOpsTransferable with Dart[Img] {
  override def apply(parameters: js.Array[Transferable]): Img = {
    val img = ImgOpsTransferable.jsArrToImg(width, parameters)
    img
  }
  def recover(parameters: js.Array[Transferable]) = apply(parameters)
}

case class PaletteMsg(override val resultId: Long) extends ImgOpsTransferable with Dart[ColorPalette] {
  override def apply(parameters: js.Array[Transferable]): ColorPalette = ImgOpsTransferable.jsArrToColorPalette(parameters)
}


// ImgOps message classes:
case class RandomRgbMsg(override val resultId: Long, override val paramRecoveryId: Long, width: Int, height: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.randomizeRGB(new Img(width, height)).asInstanceOf[Img]
  )
}

case class RandomLabMsg(override val resultId: Long, override val paramRecoveryId: Long, width: Int, height: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.randomizeLab(new Img(width, height)).asInstanceOf[Img]
  )
}

case class FlipHorizontalMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.flipHorizontal(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
  )
}

case class FlipVerticalMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.flipVertical(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
  )
}

case class Rotate90DegreesMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int, counterClockwise: Boolean) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle(
    ImgOps.rotate90Degrees(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
  )
}

case class Rotate180DegreesMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.rotate180Degrees(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
  )
}

case class RotateDegreesMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int, angle: Double) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.rotateDegrees(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), angle).asInstanceOf[Img]
  )
}

case class RotateRadiansMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int, angleRadians: Double) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.rotateRadians(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), angleRadians).asInstanceOf[Img]
  )
}

case class OverlayMsg(
                       override val resultId: Long,
                       override val paramRecoveryId: Long,
                       bgImgWidth: Int, fgImgWidth: Int,
                       bgX: Int, bgY: Int, fgX: Int, fgY: Int,
                       width: Int, height: Int ) extends MutatesImg {

  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.overlay(
      ImgOpsTransferable.jsArrToImg(bgImgWidth, parameters, 1),
      ImgOpsTransferable.jsArrToImg(fgImgWidth, parameters, 2),
      bgX, bgY, fgX, fgY, width, height).asInstanceOf[Img]
  )
}


// blur:
case class EpanechnikovBlurRGBMsg(override val resultId: Long, imgWidth: Int, radius: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.epanechnikovBlurRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), radius).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class UniformBlurRGBMsg(override val resultId: Long, imgWidth: Int, radius: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.uniformBlurRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), radius).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class GaussianBlurRGBMsg(override val resultId: Long, imgWidth: Int, radius: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.gaussianBlurRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), radius).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class UnsharpenMaskRGBMsg(override val resultId: Long, imgWidth: Int, radius: Int, amount: Double, threshold: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.unsharpenMaskRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), radius, amount, threshold).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class UnsharpenMaskLABMsg(override val resultId: Long, imgWidth: Int, radius: Int, amount: Double, threshold: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.unsharpenMaskLAB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), radius, amount, threshold).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class DifferenceMatteMsg(override val resultId: Long, override val paramRecoveryId: Long, width1: Int, width2: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.differenceMatte(
      ImgOpsTransferable.jsArrToImg(width1, parameters, 1),
      ImgOpsTransferable.jsArrToImg(width2, parameters, 2)
    ).asInstanceOf[Img]
  )
}

case class ScaleMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int, newWidth: Int, newHeight: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.scale(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), newWidth, newHeight).asInstanceOf[Img]
  )
}

case class GrayscaleAverageRGBMSG(override val resultId: Long, imgWidth: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.grayscaleAverageRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class GrayscaleLABIntensityMSG(override val resultId: Long, imgWidth: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.grayscaleLABIntensity(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class EqualizeRGBMSG(override val resultId: Long, imgWidth: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.equalizeRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class NegativeMSG(override val resultId: Long, imgWidth: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.negative(ImgOpsTransferable.jsArrToImg(imgWidth, parameters)).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class ThresholdLabMsg(override val resultId: Long, imgWidth: Int, intensity: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.thresholdLab(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), intensity).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class ThresholdRGBMsg(override val resultId: Long, imgWidth: Int, intensity: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.thresholdRGB(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), intensity).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class BrightnessMsg(override val resultId: Long, imgWidth: Int, brightness: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.brightness(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), brightness).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class ContrastMsg(override val resultId: Long, imgWidth: Int, contrast: Double) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.contrast(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), contrast).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}

case class MedianMsg(override val resultId: Long, override val paramRecoveryId: Long, imgWidth: Int, radius: Int) extends YieldsImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = bundle (
    ImgOps.median(ImgOpsTransferable.jsArrToImg(imgWidth, parameters), radius).asInstanceOf[Img]
  )
}

case class ConcisePaletteMsg(override val resultId: Long, override val paramRecoveryId: Long, width: Int) extends YieldsColorPalette {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    import ImgOpsTransferable._
    val msg = PaletteMsg(resultId)
    val cp = ImgOps.concisePalette(ImgOpsTransferable.jsArrToImg(width, parameters))
    js.Array[Transferable](
      toArrayBuffer(Pickle.intoBytes(msg.asInstanceOf[ImgOpsTransferable])),
      colorPaletteToBytes(cp)
    )
  }
}

case class ProjectToPaletteMsg(override val resultId: Long, width: Int) extends MutatesImg {
  override def apply(parameters: js.Array[Transferable]): js.Array[Transferable] = {
    ImgOps.projectToPalette(
      ImgOpsTransferable.jsArrToColorPalette(parameters, 1),
      ImgOpsTransferable.jsArrToImg(width, parameters, 2)
    ).asInstanceOf[Img]
    js.Array[Transferable]()
  }
}
