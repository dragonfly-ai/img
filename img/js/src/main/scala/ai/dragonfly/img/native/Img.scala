package ai.dragonfly.img.native

import ai.dragonfly.color._
import ai.dragonfly.img.Image

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.typedarray.Uint8ClampedArray

@JSExportTopLevel("ai.dragonfly.img.Img") class Img (override val width: Int, @JSExport val pixelData: Uint8ClampedArray) extends Image {

  override val height: Int = (pixelData.length / 4) / width

  @JSExportTopLevel("ai.dragonfly.img.Img") def this(width: Int, height: Int) = this(width, new Uint8ClampedArray(width * height * 4))

  def this(width: Int, pixelArray: Array[Int]) = this(
    width,
    {
      val ui8ca = new Uint8ClampedArray(pixelArray.length * 4)
      var j = 0
      for (i: Int <- 0 until pixelArray.length) {
        j = i * 4
        val c: RGBA = pixelArray(i)
        ui8ca(j) = c.red
        ui8ca(j+1) = c.green
        ui8ca(j+2) = c.blue
        ui8ca(j+3) = c.alpha
      }
      ui8ca
    }
  )

  override def getARGB(x:Int, y:Int): Int = {
    val index = linearIndexOf(x,y)
    RGBA(pixelData(index), pixelData(index+1), pixelData(index+2), pixelData(index+3)).argb
  }

  override def setARGB(x:Int, y:Int, argb: Int): Unit = {
    val c: RGBA = argb
    val index = linearIndexOf(x,y)
    pixelData(index) = c.red
    pixelData(index+1) = c.green
    pixelData(index+2) = c.blue
    pixelData(index+3) = c.alpha
  }

  override def linearIndexOf(x: Int, y: Int): Int = y * width + x * 4

  override def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = new Img(w, getUint8ClampedArrayPixels(xOffset, yOffset, w, h))

  override def setSubImage(xOffset: Int, yOffset: Int, sourceImage: Img, sxOffset: Int, syOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = {
    setUint8ClampedArrayPixels(xOffset, yOffset, w, h, sourceImage.getUint8ClampedArrayPixels(sxOffset, syOffset, w, h))
    this
  }

  /*
  override def getIntPixels(xOffset: Int, yOffset: Int, w: Int, h: Int): Array[Int] = {
    val arr = new Array[Int](w * h)
    var i = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        arr(i) = getARGB(xOffset + x, yOffset + y)
        i = i + 1
      }
    }
    arr
  }

  override def setIntPixels(xOffset: Int, yOffset: Int, w: Int, h: Int, pxls: Array[Int]): ai.dragonfly.img.Img = {
    var workingOffset = 0

    for (y <- yOffset until yOffset + h) {
      var xOffset = workingOffset
      for (x <- xOffset until xOffset + w) {
        val argb = pxls(xOffset)
        xOffset = xOffset + 1
        this.setARGB(x, y, argb)
      }
      workingOffset = workingOffset + w
    }

    this
  }
*/
  def getUint8ClampedArrayPixels(xOffset: Int, yOffset: Int, w: Int, h: Int): Uint8ClampedArray = {
    val arr = new Uint8ClampedArray(w * h * 4)
    var i = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        val c: RGBA = getARGB(xOffset + x, yOffset + y)
        arr(i) = c.red
        arr(i+1) = c.green
        arr(i+2) = c.blue
        arr(i+3) = c.alpha
        i = i + 4
      }
    }
    arr
  }

  def setUint8ClampedArrayPixels(xOffset: Int, yOffset: Int, w: Int, h: Int, uint8Array: Uint8ClampedArray): ai.dragonfly.img.Img = {
    var workingOffset = 0

    for (y <- yOffset until yOffset + h) {
      for (x <- xOffset until xOffset + w) {
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

  override def toString(): String = s"Img($width X $height)"

}