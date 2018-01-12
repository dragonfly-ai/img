package ai.dragonfly.img

import ai.dragonfly.color._
import ai.dragonfly.color.Color._
import ai.dragonfly.math.stats.StreamingVectorStats
import ai.dragonfly.math.stats.kernel._
import ai.dragonfly.math.vector.{Vector2, Vector3, VectorN}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ai.dragonfly.imgImgOps")
object ImgOps {

  @JSExport def randomizeRGB(img: ImageBasics): ImageBasics = {
    img pixels ( (x: Int, y: Int) => img.setARGB( x, y, Color.random().argb ) )
    img
  }

  @JSExport def flipHorizontal(img: ImageBasics): ImageBasics = {
    val flipped: ImageBasics = new Img(img.width, img.height)
    val end:Int = img.width - 1
    flipped pixels ((x: Int, y: Int) => {
      flipped.setARGB( x, y, img.getARGB(end - x, y) )
    })
    flipped
  }

  @JSExport def flipVertical(img: ImageBasics): ImageBasics = {
    val flipped: ImageBasics = new Img(img.width, img.height)
    val end:Int = img.height - 1
    img pixels ((x: Int, y: Int) => {
      flipped.setARGB( x, y, img.getARGB(x, end - y) )
    })
    flipped
  }

  @JSExport def rotate90Degrees(img: ImageBasics, counterClockwise: Boolean = false): ImageBasics = {
    val rotated: ImageBasics = new Img(img.height, img.width)
    val rotationFunction = if (counterClockwise) {
      val endY:Int = img.width - 1; (x: Int, y: Int) => rotated.setARGB(x, y, img.getARGB(endY - y, x))
    } else {
      val endX = img.height - 1; (x: Int, y: Int) => rotated.setARGB(x, y, img.getARGB(y, endX - x))
    }
    rotated pixels rotationFunction
  }

  @JSExport def rotate180Degrees (img: ImageBasics): ImageBasics = {
    val rotated: ImageBasics = new Img(img.width, img.height)
    val endX: Int = img.width - 1
    val endY: Int = img.height - 1
    rotated pixels ((x: Int, y: Int) => rotated.setARGB(x, y, img.getARGB(endX - x, endY - y)))
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

  /*
   * Creates a difference matte from the two input images.
   *
   * @param img1: Img  The dimensions of this image determines the dimensions of the output image.
   * @param img2: Img  If this image does not have the same dimensions as img1, it is scaled to fit.
   * @return ImageBasics an image representing the difference matte.

   */

  @JSExport def differenceMatte(img1: ImageBasics, img2: ImageBasics): ImageBasics = {

    val fitted = if ( img1.width != img2.width || img1.height != img2.height ) scale(img2, img1.width, img2.height)
    else img2

    val comparison = new Img( fitted.width,  fitted.height )

    img1 pixels ((x: Int, y: Int) => {
      val c1: RGBA = img1.getARGB( x, y )
      val c2: RGBA = fitted.getARGB( x, y )

      // Compute the difference
      val dif = RGBA( Math.abs ( c1.red - c2.red ), Math.abs( c1.green - c2.green ), Math.abs ( c1.blue - c2.blue ) )
      comparison.setARGB(x, y, dif)
    })

    comparison

  }

  // scale images.  Bilinear interpolation

  def scale(img: ImageBasics, newWidth: Int, newHeight: Int ): ImageBasics = {

    if (newWidth >= img.width && newHeight >= img.height) { // Bilinear interpolation to scale image up.
      val scaleX: Double = img.width / newWidth.toDouble
      val scaleY: Double = img.height / newHeight.toDouble

      val scaled: ImageBasics = new Img(newWidth, newHeight)

      scaled pixels ((u: Int, v: Int) => {
        val u1 = scaleX * u
        val v1 = scaleY * v

        val x1 = u1 - Math.floor(u * scaleX)
        val y1 = v1 - Math.floor(v * scaleY)

        val sU = u1.toInt
        val eU = Math.min(img.width - 1, u1 + 1).toInt
        val sV = v1.toInt
        val eV = Math.min(img.height - 1, v1 + 1).toInt

        val c00: RGBA = img.getARGB(sU, sV)
        val c01: RGBA = img.getARGB(sU, eV)
        val c10: RGBA = img.getARGB(eU, sV)
        val c11: RGBA = img.getARGB(eU, eV)

        val w1 = (1 - x1) * (1 - y1)
        val w2 = x1 * (1 - y1)
        val w3 = (1 - x1) * y1
        val w4 = x1 * y1

        val red: Int = (c00.red * w1 + c10.red * w2 + c01.red * w3 + c11.red * w4).toInt
        val green: Int = (c00.green * w1 + c10.green * w2 + c01.green * w3 + c11.green * w4).toInt
        val blue: Int = (c00.blue * w1 + c10.blue * w2 + c01.blue * w3 + c11.blue * w4).toInt

        scaled.setARGB(u, v, RGBA(red, green, blue))
      })
      scaled
    } else if (newWidth <= img.width && newHeight <= img.height) {  // sampling to shrink image
      val scaleX = newWidth.toDouble / img.width
      val scaleY = newHeight.toDouble / img.height

      val statsImg: Array[Array[StreamingVectorStats]] = Array.fill(newWidth, newHeight){ new StreamingVectorStats(4)  }
      img pixels ((x: Int, y: Int) => {
        val c: RGBA = img.getARGB(x, y)
        statsImg((x * scaleX).toInt)((y * scaleY).toInt)(new VectorN(c.alpha, c.red, c.green, c.blue))
      })

      val scaled: ImageBasics = new Img(newWidth, newHeight)
      scaled pixels ((x: Int, y: Int) => {
        scaled.setARGB(x, y, {
          val v: Array[Double] = statsImg(x)(y).average().values
          RGBA(v(1).toInt, v(2).toInt, v(3).toInt, v(0).toInt).argb
        })
      })
    } else { // Shrink one dimension and grow the other

      val shrinkFirst = if (newWidth < img.width) scale(img, newWidth, img.height)
      else scale(img, img.width, newHeight)

      scale(shrinkFirst, newWidth, newHeight)
    }
  }

  @JSExport def rotateDegrees(img: ImageBasics, angleDegrees: Double): ImageBasics = rotateRadians(img, angleDegrees * 0.01745329252)

  @JSExport def rotateRadians(img: ImageBasics, angleRadians: Double): ImageBasics = {
    // Step 1, assess canvas size for resulting image:
    val midpoint1 = new Vector2(img.width / 2.0, img.height / 2.0)
    val corners: Array[Vector2] = Array(
      new Vector2(0, 0).subtract(midpoint1).asInstanceOf[Vector2],
      new Vector2(0, img.height).subtract(midpoint1).asInstanceOf[Vector2],
      new Vector2(img.width, 0).subtract(midpoint1).asInstanceOf[Vector2],
      new Vector2(img.width, img.height).subtract(midpoint1).asInstanceOf[Vector2]
    )

    var minX = Double.MaxValue
    var minY = Double.MaxValue
    var maxX = Double.MinValue
    var maxY = Double.MinValue

    for ( v <- corners ) {
      val rotated = v.rotate(angleRadians).asInstanceOf[Vector2]
      minX = Math.min(rotated.x, minX)
      minY = Math.min(rotated.y, minY)
      maxX = Math.max(rotated.x, maxX)
      maxY = Math.max(rotated.y, maxY)
    }

    val rotated: ImageBasics = new Img(Math.sqrt(Math.pow(maxX - minX, 2)).toInt + 2, Math.sqrt(Math.pow(maxY - minY, 2)).toInt + 2)

    val midpoint2: Vector2 = new Vector2(rotated.width / 2.0, rotated.height / 2.0)

    // Sample pixels from img
    rotated pixels ((x: Int, y: Int) => {

      val x0 = x - midpoint2.x
      val y0 = y - midpoint2.y

      val cos = Math.cos( -angleRadians )
      val sin = Math.sin( -angleRadians )

      val u1 = (x0 * cos) + (y0 * -sin) + midpoint1.x
      val v1 = (x0 * sin) + (y0 * cos) + midpoint1.y

      if (u1 >= 0 && u1 < img.width && v1 >= 0 && v1 < img.height) {
        val x1 = u1 - Math.floor(u1)
        val y1 = v1 - Math.floor(v1)

        val sU = u1.toInt
        val eU = Math.min(img.width - 1, u1 + 1).toInt
        val sV = v1.toInt
        val eV = Math.min(img.height - 1, v1 + 1).toInt

        val c00: RGBA = img.getARGB(sU, sV)
        val c01: RGBA = img.getARGB(sU, eV)
        val c10: RGBA = img.getARGB(eU, sV)
        val c11: RGBA = img.getARGB(eU, eV)

        val w1 = (1 - x1) * (1 - y1)
        val w2 = x1 * (1 - y1)
        val w3 = (1 - x1) * y1
        val w4 = x1 * y1

        val red: Int = (c00.red * w1 + c10.red * w2 + c01.red * w3 + c11.red * w4).toInt
        val green: Int = (c00.green * w1 + c10.green * w2 + c01.green * w3 + c11.green * w4).toInt
        val blue: Int = (c00.blue * w1 + c10.blue * w2 + c01.blue * w3 + c11.blue * w4).toInt

        rotated.setARGB(x, y, RGBA(red, green, blue))
      }

    })
    rotated
  }

  @JSExport def grayscaleAverageRGB(img: ImageBasics): ImageBasics = {
    val gray: ImageBasics = new Img(img.width, img.height)
    gray.pixels((x: Int, y: Int) => {
      val c = img.getARGB(x, y)
      val avgIntensity = (c.red + c.green + c.blue) / 3
      gray.setARGB(x, y, RGBA(avgIntensity, avgIntensity, avgIntensity, c.alpha))
    })
  }

  @JSExport def grayscaleLABIntensity(img: ImageBasics): ImageBasics = {
    val gray: ImageBasics = new Img(img.width, img.height)
    gray.pixels((x: Int, y: Int) => {
      val c: RGBA = RGBA(img.getARGB(x, y))
      val lab: LAB = c
      val intensity: RGBA = SlowSlimLab(lab.L, 0f, 0f)
      gray.setARGB(x, y, RGBA(intensity.red, intensity.green, intensity.blue, c.alpha))
    })
  }

  @JSExport def equalizeRGB(img: ImageBasics): ImageBasics = {
    val redHist = Array.fill[Double](256)(0.0)
    val greenHist = Array.fill[Double](256)(0.0)
    val blueHist = Array.fill[Double](256)(0.0)

    // compute histograms:
    img pixels ((x: Int, y: Int) => {
      val c = img.getARGB(x, y)
      redHist(c.red) = redHist(c.red) + 1.0
      greenHist(c.green) = greenHist(c.green) + 1.0
      blueHist(c.blue) = blueHist(c.blue) + 1.0
    })

    val pixelCount: Double = img.width * img.height

    var redCPD:Double = 0.0
    var greenCPD:Double = 0.0
    var blueCPD:Double = 0.0

    for (i <- 0 until 256) {
      redHist(i) = redCPD + (redHist(i) / pixelCount)
      redCPD = redHist(i)
      greenHist(i) = greenCPD + (greenHist(i) / pixelCount)
      greenCPD = greenHist(i)
      blueHist(i) = blueCPD + (blueHist(i) / pixelCount)
      blueCPD = blueHist(i)
    }

    img pixels ((x: Int, y: Int) => {
      val c = img.getARGB(x, y)
      img.setARGB(x, y, RGBA(
        Math.floor(redHist(c.red) * 255).toInt,
        Math.floor(greenHist(c.green) * 255).toInt,
        Math.floor(blueHist(c.blue) * 255).toInt,
        c.alpha
      ))
    })
  }

}
