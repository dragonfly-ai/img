package ai.dragonfly.img

import ai.dragonfly.distributed.Snowflake
import org.scalajs.dom._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLImageElement

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.Uint8ClampedArray
import scalatags.JsDom.all._

/**
 * Created by clifton on 12/31/16.
 */

@JSExport
object ImgDOMUtils {

  @JSExport def canvasElement(width:Int, height:Int, id: Long = Snowflake()): Canvas = canvas(
    attr("id") := id.toString,
    //style := s"position: absolute; top:0px; left:0px; width:${width}px; height:${height}px;",
    attr("style") := s"width:${width}px; height:${height}px;",
    attr("width") := width,  // don't use: "width := width" the attribute converts to CSS and it introduces a naming conflict.
    attr("height") := height // don't use: "height := height"
  ).render

  @JSExport def toCanvas(img: Img): Canvas = {
    //println("called toCanvas")
    renderToCanvas(img, canvasElement(img.width, img.height))
  }

  @JSExport def renderToCanvas(img: Img, canvas: Canvas): Canvas = {
    println("renderToCanvas")

    val imgDat = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D].getImageData(0, 0, img.width, img.height)
    val data: Uint8ClampedArray = imgDat.data.asInstanceOf[Uint8ClampedArray]
    for (i <- 0 until img.pixelData.length) data(i) = img.pixelData(i)
    canvas
      .getContext("2d")
      .asInstanceOf[CanvasRenderingContext2D]
      .putImageData(imgDat, 0, 0)
    canvas
  }

  @JSExport def imageElement(src: String): HTMLImageElement = img( attr("src") := src ).render

  @JSExport def imageElement(width:Int, height:Int, id: String, src: String): HTMLImageElement = img(
    attr("id") := id,
    attr("width") := width,   // don't use: "width := width" the attribute converts to CSS and it introduces a naming conflict.
    attr("height") := height, // don't use: "height := height"
    attr("src") := src
  ).render

  @JSExport def toHtmlImage(canvas:Canvas): HTMLImageElement = imageElement(canvas.width, canvas.height, canvas.id, canvas.toDataURL("image/png"))

  @JSExport def toHtmlImage(img:Img): HTMLImageElement = toHtmlImage(toCanvas(img))

  private def imageDataToImg(imageData:ImageData): Img = {
    new Img(imageData.width, imageData.height).setUint8ClampedArray(
      0, 0, imageData.width, imageData.height,
      imageData.data.asInstanceOf[Uint8ClampedArray]
    ).asInstanceOf[Img]
  }

  private def imgToImageData(img: Img): ImageData = {
    val ctx = canvasElement(img.width, img.height).getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    val imageData = ctx.getImageData(0, 0, img.width, img.height)
    for (i <- 0 until imageData.data.length) {
      imageData.data(i) = img.pixelData(i)
    }
    imageData
  }

  @JSExport def canvasToImg(canvas:Canvas): Img = {
    val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    imageDataToImg(ctx.getImageData(0, 0, canvas.width, canvas.height))
  }

  @JSExport def htmlImageElementToImg(htmlImageElement: HTMLImageElement): Img = {
    val canvas: Canvas = canvasElement(htmlImageElement.naturalWidth, htmlImageElement.naturalHeight)
    val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    ctx.drawImage(
      htmlImageElement,
      0, 0, htmlImageElement.width, htmlImageElement.height,
      0, 0, canvas.width, canvas.height
    )

    imageDataToImg(
      ctx.getImageData(0, 0, canvas.width, htmlImageElement.height)
    )
  }


  @JSExport def blankImageData(width: Int, height: Int): ImageData = {
    canvasElement(width, height).getContext("2d").asInstanceOf[CanvasRenderingContext2D].getImageData(0, 0, width, height)
  }
}