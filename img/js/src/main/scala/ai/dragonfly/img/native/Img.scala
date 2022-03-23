package ai.dragonfly.img.native

import ai.dragonfly.color.*
import ai.dragonfly.img.{Image, MismatchedDimensions}

import scala.scalajs.js.typedarray.Uint8ClampedArray

class Img (override val width: Int, val pixelData: Uint8ClampedArray) extends Image {

  override val height: Int = (pixelData.length / 4) / width

  def this(width: Int, height: Int) = this(width, new Uint8ClampedArray(width * height * 4))

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

//  def pixels (f:  scala.scalajs.js.Function2[Int, Int, Any]): Img = {
//    for (y <- 0 until height) {
//      for (x <- 0 until width) {
//        f(x, y)
//      }
//    }
//    this.asInstanceOf[Img]
//  }

  inline override def linearIndexOf(x: Int, y: Int): Int = (y * width + x) * 4

  override def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = new Img(w, getPixelData(xOffset, yOffset, w, h))

  override def setSubImage(xOffset: Int, yOffset: Int, sourceImage: Img, sxOffset: Int, syOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = {
    setPixelData(xOffset, yOffset, w, h, sourceImage.getPixelData(sxOffset, syOffset, w, h))
    this
  }

  def getPixelData(xOffset: Int, yOffset: Int, w: Int, h: Int): Uint8ClampedArray = {
    val pixelData = new Uint8ClampedArray(w * h * 4)
    var j = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        val i:Int = linearIndexOf(xOffset + x, yOffset + y)
        pixelData(j) = this.pixelData(i)
        pixelData(j+1) = this.pixelData(i+1)
        pixelData(j+2) = this.pixelData(i+2)
        pixelData(j+3) = this.pixelData(i+3)
        j = j + 4
      }
    }
    pixelData
  }

  def setPixelData(pixelData:Uint8ClampedArray):ai.dragonfly.img.Img = {
    if (this.pixelData.length == pixelData.length) {
      for (i <- 0 until this.pixelData.length) this.pixelData(i) = pixelData(i)
    } else throw MismatchedDimensions(this.pixelData.length, pixelData.length)
    this
  }

  def setPixelData(xOffset: Int, yOffset: Int, w: Int, h: Int, pixelData: Uint8ClampedArray): ai.dragonfly.img.Img = {
    var j = 0

    for (y <- yOffset until yOffset + h) {
      for (x <- xOffset until xOffset + w) {
        val i = linearIndexOf(x, y)
        this.pixelData(i) = pixelData(j)
        this.pixelData(i+1) = pixelData(j+1)
        this.pixelData(i+2) = pixelData(j+2)
        this.pixelData(i+3) = pixelData(j+3)
        j = j + 4
      }
    }
    this
  }

  override def toString(): String = s"Img($width X $height)"

}