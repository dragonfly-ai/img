package ai.dragonfly.img

import ai.dragonfly.color.Color

object Img {
  def apply(width: Int, height: Int): Img = new ai.dragonfly.img.native.Img(
    width, Array.fill[Int](width * height)(Color.CLEAR.argb)
  )
}
