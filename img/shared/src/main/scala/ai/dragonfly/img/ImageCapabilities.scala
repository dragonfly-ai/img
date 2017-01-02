package ai.dragonfly.img

import scala.scalajs.js
import js.annotation.{JSExportAll, JSExport}

@JSExportAll
trait ImageCapabilities {

  val width: Int
  val height: Int

  def getRGB(x: Int, y:Int): Int
  def setRGB(x: Int, y:Int, rgba: Int): Unit

  def pixels(f: (Int, Int) => Any): ImageCapabilities = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        f(x, y)
      }
    }
    this
  }

  def pixels (f:  js.Function2[Int, Int, Any]): ImageCapabilities = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        f(x, y)
      }
    }
    this
  }
}