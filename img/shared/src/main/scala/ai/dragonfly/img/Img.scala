package ai.dragonfly.img

object Img {
  def apply(width: Int, height: Int): Img = new ai.dragonfly.img.native.Img(width, height)
}
