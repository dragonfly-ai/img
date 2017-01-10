package ai.dragonfly.img

import ai.dragonfly.color._
import ai.dragonfly.color.Color._

import scala.scalajs.js.annotation.{JSExport, JSExportAll}

trait ImageCapabilities extends ImageBasics {

  @JSExport def randomizeRGB(): ImageCapabilities = {
    this pixels ( (x: Int, y: Int) => setARGB( x, y, Color.random().argb ) )
    this
  }

  @JSExport def overlay(fgImg: ImageCapabilities,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int
  ): ImageCapabilities = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val fgc: RGBA = fgImg.getARGB(fgX + x, fgY + y)
        val bgc: RGBA = this.getARGB(bgX + x, bgY + y)
        this.setARGB(bgX + x, bgY + y, Color.alphaBlend(fgc, bgc).argb)
      }
    }
    this
  }

  @JSExport def blur()
}