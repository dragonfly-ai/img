/*
 * Copyright 2020 dragonfly.ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.dragonfly.img.native

import java.awt.image.BufferedImage

import ai.dragonfly.img.Image

import scala.language.implicitConversions

object Img {
  implicit def toBufferedImage(img: Img): BufferedImage = {
    val bi = new BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB)
    bi.setRGB(0,0, img.width, img.height, img.getPixels(0, 0, img.width, img.height), 0, img.width)
    bi
  }
  implicit def toImg(bi: BufferedImage): Img = new Img(bi.getWidth(), bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth))
}

class Img (override val width: Int, private val pixelData: Array[Int]) extends Image {
  
  def this(width: Int, height: Int) = this(width, new Array[Int](width * height))

  override val height: Int = pixelData.length / width

  override def linearIndexOf(x: Int, y: Int): Int = y * width + x

  override def getARGB(x: Int, y:Int): Int = pixelData(linearIndexOf(x, y))

  override def setARGB(x: Int, y: Int, argb: Int): Unit = pixelData(linearIndexOf(x,y)) = argb

  override def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = new ai.dragonfly.img.Img( w, getPixels(xOffset, yOffset, w, h) )

  override def setSubImage(xOffset: Int, yOffset: Int, sourceImage: Img, sxOffset: Int, syOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = {
    setPixels(xOffset, yOffset, w, h, sourceImage.getPixels(sxOffset, syOffset, w, h))
    this
  }

  override def copy(): ai.dragonfly.img.Img = new Img(
    width,
    {
      val copiedPixelData: Array[Int] = new Array(pixelData.length)
      pixelData.copyToArray(copiedPixelData)
      copiedPixelData
    }
  )

  private def getPixels(xOffset: Int, yOffset: Int, w: Int, h: Int): Array[Int] = {
    val pxls: Array[Int] = new Array[Int](w * h)
    var i = 0
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        pxls(i) = getARGB(xOffset + x, yOffset + y)
        i = i + 1
      }
    }
    pxls
  }

  private def setPixels(xOffset: Int, yOffset: Int, w: Int, h: Int, pxls: Array[Int]): Img = {
    var i = 0
    for (j <- linearIndexOf(xOffset, yOffset) until linearIndexOf(xOffset + w, yOffset + h-1) by width) {
      for (k <- j until j + w) {
        pixelData(k) = pxls(i)
        i = i + 1
      }
    }
    this
  }
}
