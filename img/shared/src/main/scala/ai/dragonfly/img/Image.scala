package ai.dragonfly.img

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.Uint8ClampedArray

trait Image {

  @JSExport def getARGB(x:Int, y:Int): Int
  @JSExport def setARGB(x:Int, y:Int, argb: Int): Unit

  @JSExport def width: Int
  @JSExport def height: Int

  @JSExport def pixels(f: (Int, Int) => Any): Image = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        f(x, y)
      }
    }
    this
  }

  @JSExport def pixels (f:  js.Function2[Int, Int, Any]): Image = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        f(x, y)
      }
    }
    this
  }

  @JSExport def linearIndexOf(x: Int, y: Int): Int

  @JSExport def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): Image

  @JSExport def getIntArray(startX: Int, startY: Int, w: Int, h: Int): Array[Int]

  @JSExport def setIntArray(startX: Int, startY: Int, w: Int, h: Int, rgba: Array[Int], offset: Int, stride: Int): Image

  @JSExport def getUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int): Uint8ClampedArray

  @JSExport def setUint8ClampedArray(startX: Int, startY: Int, w: Int, h: Int, uint8Array: Uint8ClampedArray): Image

  @JSExport def asUint8ClampedArray: Uint8ClampedArray

  @JSExport def asIntArray: Array[Int] = getIntArray(0, 0, width, height)

  @JSExport def copy(): Image

}