package ai.dragonfly.img

import java.util.UUID

import ai.dragonfly.color.Color
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
object ImageDOMUtils {

  @JSExport def canvasElement(width:Int, height:Int, uuid: UUID): Canvas = canvas(
    id := uuid.toString,
    //style := s"position: absolute; top:0px; left:0px; width:${width}px; height:${height}px;",
    style := s"width:${width}px; height:${height}px;",
    attr("width") := width,  // don't use: "width := width" the attribute converts to CSS and it introduces a naming conflict.
    attr("height") := height // don't use: "height := height"
  ).render

  @JSExport def blankCanvas(width: Int, height: Int): Canvas = {
    val uuid: UUID = UUID.randomUUID()
    //val canvas = document.getElementById(uuid.toString).asInstanceOf[Canvas]
    val canvas = canvasElement(width, height, uuid)
//    val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//    ctx.fillStyle = Color.BLACK.html()
//    ctx.fillRect(0, 0, width, height)
    canvas
  }

  @JSExport def toCanvas(img: Img): Canvas = {
    val canvas = blankCanvas(img.width, img.height)
    renderToCanvas(img, canvas)
    canvas
  }

  @JSExport def renderToCanvas(img: Img, canvas: Canvas): Unit = {
    canvas
      .getContext("2d")
      .asInstanceOf[CanvasRenderingContext2D]
      .putImageData(img.imageData, 0, 0)
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

  @JSExport def canvasToImg(canvas:Canvas): Img = {
    val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    imageDataToImg(ctx.getImageData(0, 0, canvas.width, canvas.height))
  }

  @JSExport def htmlImageElementToImg(htmlImageElement: HTMLImageElement): Img = {
    val canvas: Canvas = blankCanvas(htmlImageElement.naturalWidth, htmlImageElement.naturalHeight)
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
}