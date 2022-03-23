package ai.dragonfly.img

import ai.dragonfly.color.Color

object Img {
  def apply(width: Int, height: Int): Img = new ai.dragonfly.img.native.Img(width, height)
}
