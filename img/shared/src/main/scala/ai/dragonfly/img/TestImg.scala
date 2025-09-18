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

import ai.dragonfly.uriel.ColorContext.sRGB.{Luv, ARGB32}
import slash.stats.probability.distributions.stream.StreamingVectorStats

import scala.language.implicitConversions

object TestImg {

  def main(args: Array[String]): Unit = apply()

  import ai.dragonfly.uriel._

  def apply(): Unit = {
    val (width, height) = (11, 22)

    val i0 = Img(width, height)

    println(s"i0 dimensions: ${i0.width} ${i0.height}")
    i0 pixels ((x: Int, y: Int) => {
      i0.setARGB(x, y, ARGB32.fromRGB(Luv.random().toRGB))
    })

    val svs = new StreamingVectorStats[3]

    i0 pixels ((x: Int, y: Int) => {
      val luv: Luv = Luv.fromRGB(ARGB32(i0.getARGB(x, y)).toRGB)
      svs( luv.vec )
    })

    println(svs)

  }
}
