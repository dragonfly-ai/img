package ai.dragonfly.img.async

import java.nio.ByteBuffer

import ai.dragonfly.img.{ImageOperations, Img}
import boopickle.Default._
import boopickle.{CompositePickler, PicklerHelper}
import MsgPicklers._

/**
 * Created by clifton on 6/5/17.
 */

object ImgOpsInvocationMsg {
  def fromBytes(bb: ByteBuffer): ImgOpsInvocationMsg = Unpickle[ImgOpsInvocationMsg].fromBytes(bb)
}

trait ImgOpsInvocationMsg {
  def id: Long
  def toBytes: ByteBuffer = Pickle.intoBytes(this)
  def execute(): ImgOpsResponseMsg
}

object ImgOpsResponseMsg {
  def fromBytes(bb: ByteBuffer): ImgOpsResponseMsg = Unpickle[ImgOpsResponseMsg].fromBytes(bb)
}

trait ImgOpsResponseMsg {
  def id: Long
  def img: Img
  def toBytes: ByteBuffer = Pickle.intoBytes(this)
}

case class ImgResponseMsg (override val id: Long, override val img: Img) extends ImgOpsResponseMsg

case class MsgInvokeRandomRGB(override val id: Long, width: Int, height: Int) extends ImgOpsInvocationMsg {
  def execute(): ImgResponseMsg = new ImgResponseMsg(id, ImageOperations.randomizeRGB(new Img(width, height)).asInstanceOf[Img])
}

