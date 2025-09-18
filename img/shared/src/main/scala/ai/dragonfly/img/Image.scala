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