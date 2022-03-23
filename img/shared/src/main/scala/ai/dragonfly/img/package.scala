package ai.dragonfly

package object img {
  type Img = ai.dragonfly.img.native.Img

  case class MismatchedDimensions(d0:Int, d1:Int) extends Exception(s"Mismatched DimensionsException: $d0 != $d1")
}
