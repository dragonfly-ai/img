package ai.dragonfly.img

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

trait Image {

  @JSExport def getARGB(x:Int, y:Int): Int
  @JSExport def setARGB(x:Int, y:Int, argb: Int): Unit

  @JSExport def width: Int
  @JSExport def height: Int

  @JSExport def linearIndexOf(x: Int, y: Int): Int

  @JSExport def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): Img

  @JSExport def setSubImage(xOffset: Int, yOffset: Int, sourceImage: Img, sxOffset: Int, syOffset: Int, w: Int, h: Int): Img

  @JSExport def setSubImage(xOffset: Int, yOffset: Int, subImage: Img): Img = this.setSubImage(xOffset, yOffset, subImage, 0, 0, subImage.width, subImage.height)

  @JSExport def copy(): Img = getSubImage(0, 0, width, height)

  @JSExport def pixels(f: (Int, Int) => Any): Img = {
    var y: Int = 0
    while (y < height) {
      var x: Int = 0
      while (x < width) {
        f(x, y)
        x = x + 1
      }
      y = y + 1
    }
    this.asInstanceOf[Img]
  }

}