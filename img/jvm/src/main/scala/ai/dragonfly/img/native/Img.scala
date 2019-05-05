package ai.dragonfly.img.native

import java.awt.image.BufferedImage

import ai.dragonfly.img.Image

import scala.language.implicitConversions
import scala.scalajs.js.typedarray.Uint8ClampedArray

object Img {
  implicit def toBufferedImage(img: Img): BufferedImage = img.bi
  implicit def toImg(bi: BufferedImage): Img = new Img(bi)
}

class Img (private val bi: BufferedImage) extends Image {

  import Img._

  @Override val width: Int = bi.getWidth
  @Override val height: Int = bi.getHeight

  def this(width: Int, height: Int) = this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB))

  @Override def getARGB(x: Int, y:Int): Int = bi.getRGB(x,y)

  @Override def setARGB(x: Int, y: Int, argb: Int): Unit = bi.setRGB(x,y, argb)

  @Override def linearIndexOf(x: Int, y: Int): Int = y * width + x

  override def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): Img = {
    bi.getSubimage(xOffset, yOffset, w, h)
  }

  override def getIntArray(startX: Int, startY: Int, w: Int, h: Int): Array[Int] = bi.getRGB(startX, startY, w, h, null, 0, w)

  override def setIntArray(startX: Int, startY: Int, w: Int, h: Int, rgba: Array[Int], offset: Int, stride: Int): Img = {
    bi.setRGB(startX, startY, w, h, rgba, offset, stride)
    this
  }

  override def getUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int): Uint8ClampedArray = {
    val intPixels: Array[Int] = getIntArray(startX, startY, w, h)
    val uint8CA = new Uint8ClampedArray(4 * intPixels.length)
    var i1 = 0
    var argb = 0
    for (i <- 0 until intPixels.length) {
      i1 = 4 * i
      argb = intPixels(i)
      uint8CA(i1) = argb >> 16 & 0xff
      uint8CA(i1+1) = argb >> 8 & 0xff
      uint8CA(i1+2) = argb & 0xff
      uint8CA(i1+3) = argb >> 24 & 0xff
    }
    uint8CA
  }

  override def setUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int, uint8Array: Uint8ClampedArray): Img = {
    var workingOffset = 0

    for (y <- startY until startY + h) {
      for (x <- startX until startX + w) {
        bi.setRGB(x, y,
          uint8Array(workingOffset) << 16 |   // red
          uint8Array(workingOffset+1) << 8 |  // green
          uint8Array(workingOffset+2) |       // blue
          (uint8Array(workingOffset+3) << 24) // alpha
        )
        workingOffset = workingOffset + 4
      }
    }

    this
  }

  override def asUint8ClampedArray = getUint8ClampedArray(0, 0, width, height)

  override def copy(): Img = getSubImage(0, 0, width, height)
}