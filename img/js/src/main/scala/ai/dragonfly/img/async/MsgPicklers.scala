package ai.dragonfly.img.async

import java.nio.ByteBuffer

import ai.dragonfly.img.Img

import boopickle.Default._
import boopickle.{CompositePickler, PicklerHelper}

import scala.scalajs.js.typedarray.{TypedArrayBuffer, TypedArrayBufferOps}

/**
 * Created by clifton on 7/10/17.
 */

object MsgPicklers extends PicklerHelper {

  implicit object imgPickler extends P[Img] {

    override def pickle(img: Img)(implicit state: PickleState): Unit = {
      println("Serializing img with dimensions: " + img.width + "X" + img.height)
      state.enc.writeInt(img.width)
      state.enc.writeInt(img.height)
      state.enc.writeByteArray(TypedArrayBuffer.wrap(img.pixelData.buffer).array())
    }

    override def unpickle(implicit state: UnpickleState): Img = {
      val w = state.dec.readInt
      val h = state.dec.readInt
      println("deserializing image with dimensions: " + w + "X" + h)
      val c = new Img(w, TypedArrayBuffer.wrap(state.dec.readByteArray(w*h*4)))
      state.addIdentityRef(c)
      c
    }

  }

  //  implicit val imgPickler = transformPickler[Img, (Int, Array[Int])]((t:(Int, Array[Int])) => new Img(t._1, t._2))((img: Img) => (img.width, img.asIntArray))
  //  implicit val imgTuplePickler = transformPickler[(Int, Array[Int]), Img]((img: Img) => (img.width, img.asIntArray))((t:(Int, Array[Int])) => new Img(t._1, t._2))

  implicit val imgOpsResponseMsgPickler: CompositePickler[ImgOpsResponseMsg] = compositePickler[ImgOpsResponseMsg]
    .addConcreteType[ImgResponseMsg]

  implicit val imgOpsInvocationMsgPickler: CompositePickler[ImgOpsInvocationMsg] = compositePickler[ImgOpsInvocationMsg].
    addConcreteType[MsgInvokeRandomRGB]

}
