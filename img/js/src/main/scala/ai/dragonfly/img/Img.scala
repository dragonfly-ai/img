package ai.dragonfly.img


import ai.dragonfly.color._

import scala.scalajs.js.annotation.{JSExportAll, JSExport}
import scala.scalajs.js.typedarray.Uint8ClampedArray

@JSExport
class Img (@Override val width: Int, @JSExport val pixelData: Uint8ClampedArray) extends ImageBasics {

  @Override val height: Int = (pixelData.length / 4) / width

  @JSExport def this(width: Int, height: Int) = this(width, new Uint8ClampedArray(width * height * 4))

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

  @Override def getARGB(x:Int, y:Int): Int = {
    val index = linearIndexOf(x,y)
    RGBA(pixelData(index), pixelData(index+1), pixelData(index+2), pixelData(index+3)).argb
  }

  @Override def setARGB(x:Int, y:Int, argb: Int): Unit = {
    val c: RGBA = argb
    val index = linearIndexOf(x,y)
    pixelData(index) = c.red
    pixelData(index+1) = c.green
    pixelData(index+2) = c.blue
    pixelData(index+3) = c.alpha
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

  @JSExport def asUint8ClampedArray: Uint8ClampedArray = pixelData

  @Override override def copy(): ImageBasics = getSubImage(0, 0, width, height)
}