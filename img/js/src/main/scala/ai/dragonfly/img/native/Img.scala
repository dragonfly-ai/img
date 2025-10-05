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

import ai.dragonfly.uriel.ColorContext.sRGB.ARGB32
import ai.dragonfly.img.{Image, MismatchedDimensions}

import scala.scalajs.js.typedarray.Uint8ClampedArray

import scala.language.implicitConversions

class Img (override val width: Int, val pixelData: Uint8ClampedArray) extends Image {

  override val height: Int = (pixelData.length / 4) / width

  def this(width: Int, height: Int) = this(width, new Uint8ClampedArray(width * height * 4))

  override def getARGB(x:Int, y:Int): Int = {
    val index = linearIndexOf(x,y)
    ARGB32(pixelData(index), pixelData(index+1), pixelData(index+2), pixelData(index+3))
  }

  override def setARGB(x:Int, y:Int, argb: Int): Unit = {
    val c: ARGB32 = argb.asInstanceOf[ARGB32]
    val index = linearIndexOf(x,y)
    pixelData(index) = c.red
    pixelData(index+1) = c.green
    pixelData(index+2) = c.blue
    pixelData(index+3) = c.alpha
  }

//  def pixels (f:  scala.scalajs.js.Function2[Int, Int, Any]): Img = {
//    for (y <- 0 until height) {
//      for (x <- 0 until width) {
//        f(x, y)
//      }
//    }
//    this.asInstanceOf[Img]
//  }

  inline override def linearIndexOf(x: Int, y: Int): Int = (y * width + x) * 4

  override def getSubImage(xOffset: Int, yOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = new Img(w, getPixelData(xOffset, yOffset, w, h))

  override def setSubImage(xOffset: Int, yOffset: Int, sourceImage: Img, sxOffset: Int, syOffset: Int, w: Int, h: Int): ai.dragonfly.img.Img = {
    setPixelData(xOffset, yOffset, w, h, sourceImage.getPixelData(sxOffset, syOffset, w, h))
    this
  }

  def getPixelData(xOffset: Int, yOffset: Int, w: Int, h: Int): Uint8ClampedArray = {
    val pixelData = new Uint8ClampedArray(w * h * 4)
    var j: Int = 0
    var y: Int = 0
    while (y < h) {
      var x: Int = 0
      while (x < w) {
        val i:Int = linearIndexOf(xOffset + x, yOffset + y)
        pixelData(j) = this.pixelData(i)
        pixelData(j+1) = this.pixelData(i+1)
        pixelData(j+2) = this.pixelData(i+2)
        pixelData(j+3) = this.pixelData(i+3)
        j = j + 4
        x = x + 1
      }
      y = y + 1
    }
    pixelData
  }

  def setPixelData(pixelData:Uint8ClampedArray):ai.dragonfly.img.Img = {
    if (this.pixelData.length == pixelData.length) {
      var i: Int = 0
      while (i < this.pixelData.length) {
        this.pixelData(i) = pixelData(i)
        i = i + 1
      }
    } else throw MismatchedDimensions(this.pixelData.length, pixelData.length)
    this
  }

  def setPixelData(xOffset: Int, yOffset: Int, w: Int, h: Int, pixelData: Uint8ClampedArray): ai.dragonfly.img.Img = {
    var j: Int = 0

    var y: Int = yOffset
    while (y < yOffset + h) {
      var x: Int = xOffset
      while (x < xOffset + w) {
        val i = linearIndexOf(x, y)
        this.pixelData(i) = pixelData(j)
        this.pixelData(i+1) = pixelData(j+1)
        this.pixelData(i+2) = pixelData(j+2)
        this.pixelData(i+3) = pixelData(j+3)
        j = j + 4
        x = x + 1
      }
      y = y + 1
    }
    this
  }

  override def toString: String = s"Img($width X $height)"

}