package ai.dragonfly.img

import ai.dragonfly.color._
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.ImageData

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.Uint8ClampedArray

@JSExport
class Img (@JSExport @Override val width: Int, @JSExport @Override val height: Int) extends ImageCapabilities {

  val imageData: ImageData = CanvasUtils.blankCanvas(width, height).getContext("2d").asInstanceOf[CanvasRenderingContext2D].getImageData(0, 0, width, height)

  @JSExport @Override def getRGBA(x: Int, y: Int): Int = {
    val index = linearIndexOf(x,y,width)
    val data: Uint8ClampedArray = imageData.data.asInstanceOf[Uint8ClampedArray]
    RGBA(data(index), data(index+1), data(index+2), data(index+3)).rgba
  }

  @JSExport @Override def setRGBA(x: Int, y: Int, rgba: Int): Unit = {
    val c: RGBA = rgba
    val index = linearIndexOf(x,y,width)
    val data: Uint8ClampedArray = imageData.data.asInstanceOf[Uint8ClampedArray]
    data(index) = c.red
    data(index+1) = c.green
    data(index+2) = c.blue
    data(index+3) = c.alpha
  }

}
