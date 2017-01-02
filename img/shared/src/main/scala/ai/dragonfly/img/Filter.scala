package ai.dragonfly.img

import ai.dragonfly.color._
import ai.dragonfly.color.Color._

import scala.scalajs.js.annotation.{JSExport, JSExportAll}

@JSExport
object Filter {

  @JSExport def randomizeRGB(img: ImageCapabilities): ImageCapabilities = {
    img pixels ( (x: Int, y: Int) => img.setARGB( x, y, Color.random().argb ) )
    img
  }

  @JSExport def overlay(
    bgImg: ImageCapabilities, fgImg: ImageCapabilities,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int
  ): ImageCapabilities = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        var fgc: RGBA = fgImg.getARGB(fgX + x, fgY + y)
        var bgc: RGBA = bgImg.getARGB(bgX + x, bgY + y)
        bgImg.setARGB(bgX + x, bgY + y, Color.alphaBlend(fgc, bgc).argb)
      }
    }
    bgImg
  }
}
