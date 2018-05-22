package ai.dragonfly.img.async

import java.nio.ByteBuffer
import java.util
import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.color.ColorPalette
import ai.dragonfly.distributed.Snowflake
import ai.dragonfly.img.{Img, ImgAsync, ImgCommon}
import org.scalajs.dom
import org.scalajs.dom.raw.{Transferable, Worker}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.typedarray._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import boopickle.Default._
import ai.dragonfly.img.async.ImgOpsTransferable._

/**
 * Created by clifton on 5/30/17.
 */


@JSExport("ImgOpsClient")
object ImgOpsClient {

  // lazy because the user may not want to use asynchronous image processing.
  private lazy val imgOpsRegistry = new util.HashMap[Long, PromiseWrapper]()

  private lazy val imgWorker = new Worker("ImgWorker.js")

  // handle messages from the worker
  imgWorker.addEventListener(
    "message",
    (msg: dom.MessageEvent) => {
      msg.data match {
        case s: String => println(s)
        case jsArr: js.Array[Transferable] =>
          val bytes: ByteBuffer = jsArr(0).asInstanceOf[ArrayBuffer]

          val iotMsg = Unpickle[ImgOpsTransferable].fromBytes(bytes)

          println ( s"Client received msg: $iotMsg with ${jsArr(1)}" )

          val id = iotMsg match {
            case b: Boomerang => b.paramRecoveryId
            case d: Dart[_] => d.resultId
          }

          imgOpsRegistry.get(id) match {
            case pw: PromiseWrapper =>
              pw(iotMsg, jsArr) // fulfill the promise
              imgOpsRegistry.remove(id) // remove the promise from the registry.
            case _ => println(s"Unexpected message: $iotMsg $imgOpsRegistry")
          }
        case _ => println("Client received unknown message type.")
      }
    }
  )

  // send messages to the worker
  def apply(msg: Boomerang, transferList: js.Array[Transferable] = js.Array[Transferable]()): Future[_] = {
    //println(s"ImgOpsClient: $msg")
    val resultPromiseWrapper: PromiseWrapper = msg.promiseWrapper
    imgOpsRegistry.put(msg.resultId, resultPromiseWrapper)
    //println(s"imgOpsRegistry: $imgOpsRegistry")

    val bytes: ArrayBuffer = Pickle.intoBytes(msg.asInstanceOf[ImgOpsTransferable])
    val msgPayload = js.Array[Any](bytes)

    for (t <- transferList) msgPayload.push(t)
    //println(msgPayload)
    imgWorker.postMessage(msgPayload, transferList)

    resultPromiseWrapper.p.future
  }

  // recovers transferable data sent as parameters to image operations
  def recoverParamsFor(msg: Boomerang): Future[js.Array[Transferable]] = {
    val paramReturnPromiseWrapper: PromiseWrapper = ParamPromise(Promise[js.Array[Transferable]])
    imgOpsRegistry.put(msg.paramRecoveryId, paramReturnPromiseWrapper)
    paramReturnPromiseWrapper.p.future.asInstanceOf[Future[js.Array[Transferable]]]
  }

  // Expose asynchronous image processing operations that return Img to javascript.
  def jsCallBackImgHandler(imgFuture: Future[ImgAsync], callback: js.Function1[ImgAsync, Any]): Unit = {
    imgFuture onComplete {
      case Success(img: ImgAsync) =>
        callback(img)
      case Success(_) => println("Unexpected response: " + _)
      case Failure(t) => println("failed" + t)
    }
  }

  // Expose asynchronous image processing operations that return Unit to javascript.
  def jsCallBackUnitHandler(img: ImgAsync, unitFuture: Future[js.Array[Transferable]], callback: js.Function1[ImgAsync, Any]): Unit = {
    unitFuture onComplete {
      case Success(parameters: js.Array[Transferable]) => callback(img)
      case Failure(t) => println("failed" + t)
    }
  }

  // Handle asynchronous image processing operations that mutate the original image data.
  def mutate1(msg: Boomerang, img: ImgAsync): Future[js.Array[Transferable]] = {
    val resultPromise = Promise[js.Array[Transferable]]

    for {
      pd <- img.checkOutPixelData
      parameters <- ImgOpsClient(msg, js.Array[Transferable](pd.buffer)).asInstanceOf[Future[js.Array[Transferable]]]
    } yield {
      img.checkInPixelData(ImgOpsTransferable.jsArrToImg(img.width, parameters).pixelData)
      resultPromise.success(parameters)
    }

    resultPromise.future
  }

  // Handle asynchronous image processing operations that mutate the original image data and take two img parameters.
  def mutate2(msg: Boomerang, img1: ImgAsync, img2: ImgAsync): Future[js.Array[Transferable]] = {
    val resultPromise = Promise[js.Array[Transferable]]

    for {
      pd1 <- img1.checkOutPixelData
      pd2 <- img2.checkOutPixelData
      parameters <- ImgOpsClient(msg, js.Array[Transferable](pd1.buffer, pd2.buffer)).asInstanceOf[Future[js.Array[Transferable]]]
    } yield {
      img1.checkInPixelData(ImgOpsTransferable.jsArrToImg(img1.width, parameters, 1).pixelData)
      img2.checkInPixelData(ImgOpsTransferable.jsArrToImg(img2.width, parameters, 2).pixelData)
      resultPromise.success(parameters)
    }

    resultPromise.future
  }

  // Handle immutable asynchronous image processing operations that return new image data.
  def spawn0(msg: Boomerang): Future[ImgAsync] = {
    val resultPromise = Promise[ImgAsync]

    for {
      img <- ImgOpsClient(msg, js.Array[Transferable]()).asInstanceOf[Future[Img]]
    } yield {
      println(s"spawn0 $msg with $img")
      resultPromise.success( new ImgAsync(img.width, img.pixelData) )
    }

    resultPromise.future
  }

  // Handle immutable asynchronous image processing operations that return new image data.
  def spawn1(msg: Boomerang, img: ImgAsync): Future[ImgAsync] = {
    val resultPromise = Promise[ImgAsync]

    for {
      pd <- img.checkOutPixelData
      img <- ImgOpsClient(msg, js.Array[Transferable](pd.buffer)).asInstanceOf[Future[Img]]
    } yield resultPromise.success( new ImgAsync(img.width, img.pixelData) )

    ImgOpsClient recoverParamsFor msg onComplete {
      case Success(parameters: js.Array[_]) =>
        img.checkInPixelData(ImgOpsTransferable.jsArrToImg(img.width, parameters).pixelData)
      case Failure(t) => println(s"Could not recover parameters for $msg")
    }

    resultPromise.future
  }

  def spawn2(msg: Boomerang, img1: ImgAsync, img2: ImgAsync): Future[ImgAsync] = {
    val resultPromise = Promise[ImgAsync]

    for {
      pd1 <- img1.checkOutPixelData
      pd2 <- img2.checkOutPixelData
      img <- ImgOpsClient(
        msg,
        js.Array[Transferable](
          pd1.buffer,
          pd2.buffer
        )
      ).asInstanceOf[Future[Img]]
    } yield {
      resultPromise.success(new ImgAsync(img))
    }

    ImgOpsClient recoverParamsFor msg onComplete {
      case Success(parameters: js.Array[_]) =>
        img1.checkInPixelData(ImgOpsTransferable.jsArrToImg(img1.width, parameters, 1).pixelData)
        img2.checkInPixelData(ImgOpsTransferable.jsArrToImg(img2.width, parameters, 2).pixelData)
      case Failure(t) => println(s"Could not recover parameters for $msg")
    }

    resultPromise.future
  }

  // operations:
  def randomizeRGB(width: Int, height: Int): Future[ImgAsync] = spawn0 (
    RandomRgbMsg(Snowflake(), Snowflake(), width, height)
  )
  @JSExport def randomizeRGB(width: Int, height: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(randomizeRGB(width, height), callback)

  def randomizeLab(width: Int, height: Int): Future[ImgAsync] = spawn0 (
    RandomLabMsg(Snowflake(), Snowflake(), width, height)
  )
  @JSExport def randomizeLab(width: Int, height: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(randomizeLab(width, height), callback)

  def flipHorizontal(img: ImgAsync): Future[ImgAsync] = spawn1 ( FlipHorizontalMsg( Snowflake(), Snowflake(), img.width ), img )
  @JSExport def flipHorizontal(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(flipHorizontal(img), callback)


  def flipVertical(img: ImgAsync): Future[ImgAsync] = spawn1 ( FlipVerticalMsg( Snowflake(), Snowflake(), img.width ), img )
  @JSExport def flipVertical(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(flipVertical(img), callback)

  def rotate90Degrees(img: ImgAsync, counterClockwise: Boolean = false): Future[ImgAsync] = spawn1 (
    Rotate90DegreesMsg(Snowflake(), Snowflake(), img.width, counterClockwise),
    img
  )
  @JSExport def rotate90Degrees(img: ImgAsync, counterClockwise: Boolean, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(rotate90Degrees(img, counterClockwise), callback)

  def rotate180Degrees(img: ImgAsync): Future[ImgAsync] = spawn1 (
    Rotate180DegreesMsg(Snowflake(), Snowflake(), img.width),
    img
  )
  @JSExport def rotate180Degrees(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(rotate180Degrees(img), callback)

  def rotateDegrees(img: ImgAsync, angleDegrees: Double): Future[ImgAsync] = spawn1 (
    RotateDegreesMsg(Snowflake(), Snowflake(), img.width, angleDegrees),
    img
  )
  @JSExport def rotateDegrees(img1: ImgAsync, angleDegrees: Double, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(rotateDegrees(img1, angleDegrees), callback)

  def rotateRadians(img: ImgAsync, angleRadians: Double): Future[ImgAsync] = spawn1 (
    RotateRadiansMsg(Snowflake(), Snowflake(), img.width, angleRadians),
    img
  )
  @JSExport def rotateRadians(img1: ImgAsync, angleRadians: Double, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(rotateRadians(img1, angleRadians), callback)

  def overlay(
    bgImg: ImgAsync, fgImg: ImgAsync,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int
  ): Future[js.Array[Transferable]] = mutate2 (
    OverlayMsg(Snowflake(), Snowflake(), bgImg.width, fgImg.width, bgX, bgY, fgX, fgY, width, height),
    bgImg, fgImg
  )
  @JSExport def overlay(
    bgImg: ImgAsync, fgImg: ImgAsync,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int,
    callback: js.Function1[ImgAsync, Any]
  ): Unit = jsCallBackUnitHandler( bgImg, overlay( bgImg, fgImg, bgX, bgY, fgX, fgY, width, height ), callback )

  def differenceMatte(img1: ImgAsync, img2: ImgAsync): Future[ImgAsync] = spawn2 (
    DifferenceMatteMsg(Snowflake(), Snowflake(), img1.width, img2.width),
    img1, img2
  )
  @JSExport def differenceMatte(img1: ImgAsync, img2: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(differenceMatte(img1, img2), callback)


  def epanechnikovBlurRGB(img: ImgAsync, radius: Int): Future[js.Array[Transferable]] = mutate1 (
    EpanechnikovBlurRGBMsg(Snowflake(), img.width, radius),
    img
  )
  @JSExport def epanechnikovBlurRGB(img: ImgAsync, radius: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, epanechnikovBlurRGB(img, radius), callback)

  def uniformBlurRGB(img: ImgAsync, radius: Int): Future[js.Array[Transferable]] = mutate1 (
    UniformBlurRGBMsg(Snowflake(), img.width, radius),
    img
  )
  @JSExport def uniformBlurRGB(img: ImgAsync, radius: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, uniformBlurRGB(img, radius), callback)

  def gaussianBlurRGB(img: ImgAsync, radius: Int): Future[js.Array[Transferable]] = mutate1 (
    GaussianBlurRGBMsg(Snowflake(), img.width, radius),
    img
  )
  @JSExport def gaussianBlurRGB(img: ImgAsync, radius: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, gaussianBlurRGB(img, radius), callback)

  def unsharpenMaskRGB(img: ImgAsync, radius: Int, amount: Double, threshold: Int): Future[js.Array[Transferable]] = mutate1 (
    UnsharpenMaskRGBMsg(Snowflake(), img.width, radius, amount, threshold),
    img
  )
  @JSExport def unsharpenMaskRGB(img: ImgAsync, radius: Int, amount: Double, threshold: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, unsharpenMaskRGB(img, radius, amount, threshold), callback)

  def unsharpenMaskLAB(img: ImgAsync, radius: Int, amount: Double, threshold: Int): Future[js.Array[Transferable]] = mutate1 (
    UnsharpenMaskLABMsg(Snowflake(), img.width, radius, amount, threshold),
    img
  )
  @JSExport def unsharpenMaskLAB(img: ImgAsync, radius: Int, amount: Double, threshold: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, unsharpenMaskLAB(img, radius, amount, threshold), callback)

  def median(img: ImgAsync, radius: Int): Future[ImgAsync] = spawn1 (
    MedianMsg(Snowflake(), Snowflake(), img.width, radius),
    img
  )
  @JSExport def median(img: ImgAsync, radius: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(median(img, radius), callback)


  def grayscaleAverageRGB(img: ImgAsync): Future[js.Array[Transferable]] = mutate1 (
    GrayscaleAverageRGBMSG(Snowflake(), img.width),
    img
  )
  @JSExport def grayscaleAverageRGB(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, grayscaleAverageRGB(img), callback)

  def grayscaleLABIntensity(img: ImgAsync): Future[js.Array[Transferable]] = mutate1 (
    GrayscaleLABIntensityMSG(Snowflake(), img.width),
    img
  )
  @JSExport def grayscaleLABIntensity(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, grayscaleLABIntensity(img), callback)

  def equalizeRGB(img: ImgAsync): Future[js.Array[Transferable]] = mutate1 (
    EqualizeRGBMSG(Snowflake(), img.width),
    img
  )
  @JSExport def equalizeRGB(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, equalizeRGB(img), callback)

  def negative(img: ImgAsync): Future[js.Array[Transferable]] = mutate1 (
    NegativeMSG(Snowflake(), img.width),
    img
  )
  @JSExport def negative(img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, negative(img), callback)

  def thresholdRGB(img: ImgAsync, intensity: Int): Future[js.Array[Transferable]] = mutate1 (
    ThresholdRGBMsg(Snowflake(), img.width, intensity),
    img
  )
  @JSExport def thresholdRGB(img: ImgAsync, intensity: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, thresholdRGB(img, intensity), callback)

  def thresholdLab(img: ImgAsync, intensityRGB: Int): Future[js.Array[Transferable]] = mutate1 (
    ThresholdLabMsg(Snowflake(), img.width, intensityRGB),
    img
  )
  @JSExport def thresholdLab(img: ImgAsync, intensityRGB: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, thresholdLab(img, intensityRGB), callback)

  def contrast(img: ImgAsync, intensityRGB: Int): Future[js.Array[Transferable]] = mutate1 (
    ContrastMsg(Snowflake(), img.width, intensityRGB),
    img
  )
  @JSExport def contrast(img: ImgAsync, intensityRGB: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, contrast(img, intensityRGB), callback)

  def brightness(img: ImgAsync, brightnessDifferential: Int): Future[js.Array[Transferable]] = mutate1 (
    BrightnessMsg(Snowflake(), img.width, brightnessDifferential),
    img
  )
  @JSExport def brightness(img: ImgAsync, brightnessDifferential: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, brightness(img, brightnessDifferential), callback)

  def scale(img: ImgAsync, newWidth: Int, newHeight: Int): Future[ImgAsync] = spawn1 (
    ScaleMsg(Snowflake(), Snowflake(), img.width, newWidth, newHeight),
    img
  )
  @JSExport def scale(img1: ImgAsync, newWidth: Int, newHeight: Int, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackImgHandler(scale(img1, newWidth, newHeight), callback)

  def concisePalette(img: ImgAsync): Future[ColorPalette] = {
    val msg = ConcisePaletteMsg(Snowflake(), Snowflake(), img.width)
    val resultPromise = Promise[ColorPalette]

    for {
      pd <- img.checkOutPixelData
      cp: ColorPalette <- ImgOpsClient(msg, js.Array[Transferable](pd.buffer)).asInstanceOf[Future[ColorPalette]]
    } yield {

      resultPromise.success(cp)
    }

    ImgOpsClient recoverParamsFor msg onComplete {
      case Success(parameters: js.Array[_]) =>
        img.checkInPixelData(ImgOpsTransferable.jsArrToImg(img.width, parameters).pixelData)
      case Failure(t) => println(s"Could not recover parameters for $msg")
    }

    resultPromise.future
  }
  @JSExport def concisePalette(img1: ImgAsync, callback: js.Function1[ColorPalette, Any]): Unit = {
    concisePalette(img1) onComplete {
      case scala.util.Success(palette: ColorPalette) => callback(palette)
      case scala.util.Success(_) => println("Unexpected response: " + _)
      case Failure(t) => println("failed" + t)
    }
  }

  def projectToPalette(palette: ColorPalette, img: ImgAsync): Future[js.Array[Transferable]] = {
    val msg = ProjectToPaletteMsg(Snowflake(), img.width)

    val resultPromise = Promise[js.Array[Transferable]]

    for {
      pd <- img.checkOutPixelData
      parameters <- ImgOpsClient(
        msg,
        js.Array[Transferable](
          ImgOpsTransferable.colorPaletteToBytes(palette),  // the palette
          pd.buffer // the image
        )
      ).asInstanceOf[Future[js.Array[Transferable]]]
    } yield {
      img.checkInPixelData(ImgOpsTransferable.jsArrToImg(img.width, parameters, 2).pixelData)
      resultPromise.success(parameters)
    }

    resultPromise.future
  }
  @JSExport def projectToPalette(palette: ColorPalette, img: ImgAsync, callback: js.Function1[ImgAsync, Any]): Unit = jsCallBackUnitHandler(img, projectToPalette(palette, img), callback)

}
