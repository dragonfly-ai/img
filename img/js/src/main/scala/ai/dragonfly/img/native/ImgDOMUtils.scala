package ai.dragonfly.img.native

import ai.dragonfly.img.async.AsyncImg
import org.scalajs.dom
import dom.{CanvasRenderingContext2D, HTMLImageElement, html, window}
import html.*

import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSGlobal, JSImport}
import scala.scalajs.js.typedarray.Uint8ClampedArray
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

/**
 * Created by clifton on 12/31/16.
 */

object ImgDOMUtils {

  lazy val r:scala.util.Random = scala.util.Random()

  private def randomCanvasId:String = s"Canvas${r.nextLong}"

  def canvasElement(width:Int, height:Int, id: String = randomCanvasId): Canvas = {
    val canvasTag = dom.document.createElement("canvas").asInstanceOf[Canvas]
    canvasTag.width = width
    canvasTag.height = height
    canvasTag.id = id
    canvasTag
  }

  def toCanvas(img: Img): Canvas = renderToCanvas(
    img,
    canvasElement(img.width, img.height)
  )

  def renderToCanvas(img: Img, canvas: Canvas): Canvas = {
    canvas
      .getContext("2d")
      .asInstanceOf[CanvasRenderingContext2D]
      .putImageData(
        new ImageData(img.pixelData, img.width, img.height),
        0, 0
      )

    canvas
  }

  def imageElement(src: String): HTMLImageElement = {
    val imgTag = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    imgTag.src = src
    imgTag
  }

  def preciseImageElement(width:Int, height:Int, id: String, src: String): HTMLImageElement = {
    val imgTag:HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    imgTag.width = width
    imgTag.height = height
    imgTag.id = id
    imgTag.src = src
    imgTag
  }

  def toHtmlImage(canvas:Canvas): HTMLImageElement = preciseImageElement(canvas.width, canvas.height, canvas.id, canvas.toDataURL("image/png"))

  def toHtmlImage(img:Img): HTMLImageElement = toHtmlImage(toCanvas(img))

  def toHtmlImage(aImg: AsyncImg): Future[HTMLImageElement] = aImg.reserveImgData[HTMLImageElement]( img => toHtmlImage( img ) )

  private def imageDataToImg(imageData:dom.ImageData): Img = new Img(
    imageData.width,
    imageData.data.asInstanceOf[Uint8ClampedArray]
  )

  private def imgToImageData(img: Img): dom.ImageData = {
    val ctx = canvasElement(img.width, img.height).getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    val imageData = ctx.getImageData(0, 0, img.width, img.height)
    for (i <- 0 until imageData.data.length) {
      imageData.data(i) = img.pixelData(i)
    }
    imageData
  }

  def canvasToImg(canvas:Canvas): Img = imageDataToImg(
    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D].getImageData(
      0, 0, canvas.width, canvas.height
    )
  )

  def htmlImageElementToCanvas(htmlImageElement: HTMLImageElement): Canvas = {
    val canvas: Canvas = canvasElement(htmlImageElement.naturalWidth, htmlImageElement.naturalHeight, s"Canvas${htmlImageElement.id}")

    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D].drawImage(
      htmlImageElement,
      0, 0, htmlImageElement.naturalWidth, htmlImageElement.naturalHeight,
      0, 0, htmlImageElement.naturalWidth, htmlImageElement.naturalHeight
    )
    canvas
  }

  def htmlImageElementToImg(htmlImageElement: HTMLImageElement): Img = canvasToImg(
    htmlImageElementToCanvas(htmlImageElement)
  )

  def htmlImageElementToAsyncImg(htmlImageElement: HTMLImageElement): AsyncImg = {
    val img = htmlImageElementToImg(htmlImageElement)
    new AsyncImg(new Img(img.width, img.pixelData))
  }

  def blankImageData(width: Int, height: Int): dom.ImageData = {
    val data = new Uint8ClampedArray(width * height * 4)
    for (i <- 3 until data.length by 4) { data(i) = 0xff }
    new ImageData(data, width, height )
  }
}

@js.native @JSGlobal
class ImageData extends dom.ImageData {

  def this(data: Uint8ClampedArray, width: Int) = this()
  def this(data: Uint8ClampedArray, width: Int, height: Int) = this()

}