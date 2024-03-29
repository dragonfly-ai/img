import ai.dragonfly.color.*
import ai.dragonfly.color.Color.toLab
import ai.dragonfly.img.native.Img
import ai.dragonfly.img.native.ImgDOMUtils.*
import org.scalajs.dom
import dom.{HTMLImageElement, document}

import scala.scalajs.js.typedarray.Uint8ClampedArray

object Demo {

  def main(args: Array[String]): Unit = {

    dom.window.addEventListener("load", ev => {
      val body = dom.window.document.body

      // Load Image into DOM
      val beachKidElement:HTMLImageElement = imageElement("./image/BeachKid.png")
      body.appendChild(beachKidElement).addEventListener("load", e => {
        // Read Img instance from DOM
        val img = htmlImageElementToImg(beachKidElement)

        // Write Img instance to DOM
        body.appendChild(toHtmlImage(img))

        // Write processed Img instance to DOM
        body.appendChild(toHtmlImage(img pixels { (x, y) => img.setARGB(x, y, SlowSlimLab(toLab(RGBA(img.getARGB(x,y))).L, 0, 0).argb) }))
      })

      val caliCarabidElement:HTMLImageElement = preciseImageElement(400, 272, "CaliCarabid", "./image/CaliCarabid.png")
      body.appendChild(caliCarabidElement).addEventListener("load", e => {

        // Read Canvas instance from DOM
        val canvas = htmlImageElementToCanvas(caliCarabidElement)

        // Write Canvas to DOM
        body.appendChild(canvas)

        val logoElement:HTMLImageElement = imageElement("./image/Logo.png")
        body.appendChild(logoElement).addEventListener("load", e => {
          val aImg = htmlImageElementToAsyncImg(logoElement)
          aImg.reserveImgData(
            img => {
              renderToCanvas(img.getSubImage(250, 30, 130, canvas.height), canvas)
            }
          )
        })

      })


      body.appendChild(
        toHtmlImage(
          new Img(
            400,
            blankImageData(400, 10).data.asInstanceOf[Uint8ClampedArray]
          )
        )
      )

    })
  }



}
