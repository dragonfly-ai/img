package ai.dragonfly.img

import ai.dragonfly.color.*
import ai.dragonfly.math.stats.probability.distributions.stream.StreamingVectorStats
import ai.dragonfly.math.vector.Vector3

object TestImg {

  def main(args: Array[String]): Unit = apply()

  import ai.dragonfly.color.Color._

  def apply(): Unit = {
    val (width, height) = (11, 22)

    val i0 = Img(width, height)

    println(s"i0 dimensions: ${i0.width} ${i0.height}")
    i0 pixels ((x: Int, y: Int) => {
      i0.setARGB(x, y, LAB.random())
    })

    val svs = new StreamingVectorStats(3)

    i0 pixels ((x: Int, y: Int) => {
      val c: LAB = RGBA(i0.getARGB(x, y))
      svs( Vector3(c.L, c.a, c.b) )
    })

    println(svs)

  }
}
