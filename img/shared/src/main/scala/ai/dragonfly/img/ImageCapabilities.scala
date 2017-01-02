package ai.dragonfly.img

import ai.dragonfly.color.RGBA

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.Uint8ClampedArray

trait ImageCapabilities {

  @JSExport def getARGB(x:Int, y:Int): Int
  @JSExport def setARGB(x:Int, y:Int, rgba: Int): Unit

  @JSExport def width: Int
  @JSExport def height: Int

  @JSExport def pixels(f: (Int, Int) => Any): ImageCapabilities = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        f(x, y)
      }
    }
    this
  }

  @JSExport def pixels (f:  js.Function2[Int, Int, Any]): ImageCapabilities = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        f(x, y)
      }
    }
    this
  }

  @JSExport def linearIndexOf(x: Int, y: Int, width: Int): Int

  @JSExport def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): ImageCapabilities = {
    val subImg = new Img(w, h)
    subImg pixels ((x:Int, y:Int) => subImg.setARGB(x, y, getARGB(xOffset + x, yOffset + y)))
    subImg
  }

  @JSExport def getIntArray(startX: Int, startY: Int, w: Int, h: Int): Array[Int] = {
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

  @JSExport def setIntArray(startX: Int, startY: Int, w: Int, h: Int, rgba: Array[Int], offset: Int, stride: Int): ImageCapabilities = {
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

  @JSExport def getUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int): Uint8ClampedArray = {
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

  @JSExport("setByteArray") def setUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int, uint8Array: Uint8ClampedArray): ImageCapabilities = {
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