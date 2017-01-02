package ai.dragonfly.img

import ai.dragonfly.color._
import ai.dragonfly.color.Color._


class Img (@Override val width: Int, @Override val height: Int) extends ImageCapabilities {

  val imageData: Array[Int] = new Array[Int](width*height)

  @Override def getARGB(x: Int, y:Int): Int = imageData( linearIndexOf( x, y, width ) )

  @Override def setARGB(x: Int, y: Int, argb: Int): Unit = imageData(linearIndexOf(x,y,width)) = argb

  @Override def linearIndexOf(x: Int, y: Int, width: Int): Int = y * width + x
}