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
