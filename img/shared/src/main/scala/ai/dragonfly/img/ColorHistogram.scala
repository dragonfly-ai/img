package ai.dragonfly.img

import ai.dragonfly.math.stats.DiscreteHistogram

object ColorHistogram {

  def fromImage(img: ImageBasics): DiscreteHistogram[Int] = {
    val hist = new DiscreteHistogram[Int]
    img pixels ((x: Int, y: Int) => {
      hist.adjust(0xff000000 | img.getARGB(x, y), 1)
    })
    hist
  }

}