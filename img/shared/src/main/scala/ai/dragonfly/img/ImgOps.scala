package ai.dragonfly.img

import ai.dragonfly.color._
import ai.dragonfly.color.Color._
import ai.dragonfly.math.stats.StreamingVectorStats
import ai.dragonfly.math.stats.kernel._
import ai.dragonfly.math.vector.Vector3

import scala.scalajs.js.annotation.{JSExport, JSExportAll}

@JSExport("ImgOps")
object ImgOps {

  @JSExport def randomizeRGB(img: ImageBasics): ImageBasics = {
    img pixels ( (x: Int, y: Int) => img.setARGB( x, y, Color.random().argb ) )
    img
  }

  @JSExport def overlay(
    bgImg: ImageBasics,
    fgImg: ImageBasics,
    bgX: Int, bgY: Int, fgX: Int, fgY: Int,
    width: Int, height: Int
  ): ImageBasics = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val fgc: RGBA = fgImg.getARGB(fgX + x, fgY + y)
        val bgc: RGBA = bgImg.getARGB(bgX + x, bgY + y)
        bgImg.setARGB(bgX + x, bgY + y, Color.alphaBlend(fgc, bgc).argb)
      }
    }
    bgImg
  }


  @JSExport def epanechnikovBlurRGB(toBlur: ImageBasics, radius: Int): ImageBasics = kernelBlurRGB(toBlur, EpanechnikovKernel(radius))

  @JSExport def uniformBlurRGB(toBlur: ImageBasics, radius: Int): ImageBasics = kernelBlurRGB(toBlur, UniformKernel(radius))

  @JSExport def gaussianBlurRGB(toBlur: ImageBasics, radius: Int): ImageBasics = kernelBlurRGB(toBlur, GaussianKernel(radius))


  @JSExport def kernelBlurRGB(toBlur: ImageBasics, kernel: Kernel): ImageBasics = {
    val dk = kernel.discretize

    val width:Int = toBlur.width
    val height:Int = toBlur.height

    val temp = new Img(width, height)
    val r: Int = Math.ceil(dk.radius).toInt

    val vectorStats = new StreamingVectorStats(3)

    // First Pass
    toBlur pixels ((x: Int, y: Int) => {
      for ( xi: Int <- Math.max(0, x - r) until Math.min(width, x + r + 1) ) {
        val dx = xi - x
        val c: RGBA = toBlur.getARGB(xi, y)
        vectorStats(new Vector3(c.red, c.green, c.blue), dk.weight(dx*dx))
      }
      val avg: Vector3 = vectorStats.average().asInstanceOf[Vector3]
      temp.setARGB(x, y, RGBA(avg.x.toInt, avg.y.toInt, avg.z.toInt).argb)
      vectorStats.reset()
    })

    toBlur pixels ((x: Int, y: Int) => {
      for ( yi <- Math.max(0, y - r) until Math.min(height, y + r + 1) ) {
        val dy = yi - y
        val c: RGBA = temp.getARGB(x, yi)
        vectorStats(new Vector3(c.red, c.green, c.blue), dk.weight(dy*dy))
      }
      val avg: Vector3 = vectorStats.average().asInstanceOf[Vector3]
      toBlur.setARGB(x, y, RGBA(avg.x.toInt, avg.y.toInt, avg.z.toInt).argb)
      vectorStats.reset()
    })

    toBlur
  }

}