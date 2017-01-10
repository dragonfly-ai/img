package ai.dragonfly.img

import ai.dragonfly.color._
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.ImageData

import scala.scalajs.js.annotation.{JSExportAll, JSExport}
import scala.scalajs.js.typedarray.Uint8ClampedArray

@JSExport
class Img (@Override val width: Int, @Override val height: Int) extends ImageCapabilities {

  val imageData: ImageData = ImageDOMUtils.blankCanvas(width, height).getContext("2d").asInstanceOf[CanvasRenderingContext2D].getImageData(0, 0, width, height)

  @Override def getARGB(x:Int, y:Int): Int = {
    val index = linearIndexOf(x,y)
    val data: Uint8ClampedArray = imageData.data.asInstanceOf[Uint8ClampedArray]
    RGBA(data(index), data(index+1), data(index+2), data(index+3)).argb
  }

  @Override def setARGB(x:Int, y:Int, argb: Int): Unit = {
    val c: RGBA = argb
    val index = linearIndexOf(x,y)
    val data: Uint8ClampedArray = imageData.data.asInstanceOf[Uint8ClampedArray]
    data(index) = c.red
    data(index+1) = c.green
    data(index+2) = c.blue
    data(index+3) = c.alpha
  }

  @Override def linearIndexOf(x: Int, y: Int): Int = (y * width + x) * 4




  @Override def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): ImageBasics = {
    val subImg = new Img(w, h)
    subImg pixels ((x:Int, y:Int) => subImg.setARGB(x, y, getARGB(xOffset + x, yOffset + y)))
    subImg
  }

  @Override def getIntArray(startX: Int, startY: Int, w: Int, h: Int): Array[Int] = {
    val arr = new Array[Int](w * h)
    var i = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        arr(i) = getARGB(startX + x, startY + y)
        i = i + 1
      }
    }
    arr
  }

  @Override def setIntArray(startX: Int, startY: Int, w: Int, h: Int, rgba: Array[Int], offset: Int, stride: Int): ImageBasics = {
    var workingOffset = offset

    for (y <- startY until startY + h) {
      var xOffset = workingOffset
      for (x <- startX until startX + w) {
        val argb = rgba(xOffset)
        xOffset = xOffset + 1
        this.setARGB(x, y, argb)
      }
      workingOffset = workingOffset + stride
    }

    this
  }

  @Override def getUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int): Uint8ClampedArray = {
    val arr = new Uint8ClampedArray(w * h * 4)
    var i = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        val c: RGBA = getARGB(startX + x, startY + y)
        arr(i) = c.red
        arr(i+1) = c.green
        arr(i+2) = c.blue
        arr(i+3) = c.alpha
        i = i + 4
      }
    }
    arr
  }

  @Override def setUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int, uint8Array: Uint8ClampedArray): ImageBasics = {
    var workingOffset = 0

    for (y <- startY until startY + h) {
      for (x <- startX until startX + w) {
        val argb = RGBA(
          uint8Array(workingOffset),
          uint8Array(workingOffset+1),
          uint8Array(workingOffset+2),
          uint8Array(workingOffset+3)
        ).argb
        workingOffset = workingOffset + 4
        setARGB(x, y, argb)
      }
    }

    this
  }

}
