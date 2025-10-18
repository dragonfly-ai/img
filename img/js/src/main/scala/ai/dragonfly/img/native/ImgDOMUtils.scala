/*
 * Copyright 2020 dragonfly.ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.dragonfly.img.native

import ai.dragonfly.img.async.AsyncImg

import org.scalajs.dom.{CanvasRenderingContext2D, HTMLImageElement, ImageData, document}
import org.scalajs.dom.html.Canvas

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8ClampedArray

/**
 * Created by clifton on 12/31/16.
 */

object ImgDOMUtils {

  lazy val r:scala.util.Random = scala.util.Random()

  private def randomCanvasId:String = s"Canvas${r.nextLong}"

  def canvasElement(width:Int, height:Int, id: String = randomCanvasId): Canvas = {
    val canvasTag = document.createElement("canvas").asInstanceOf[Canvas]
    canvasTag.width = width
    canvasTag.height = height
    canvasTag.id = id
    canvasTag
  }

  def toCanvas(img: Img): Canvas = renderToCanvas(
    img,
    canvasElement(img.width, img.height)
  )

  def renderToCanvas(img: Img, canvas: Canvas): Canvas = {
    canvas
      .getContext("2d")
      .asInstanceOf[CanvasRenderingContext2D]
      .putImageData(
        new ImageData(img.pixelData, img.width, img.height),
        0, 0
      )

    canvas
  }

  def imageElement(src: String): HTMLImageElement = {
    val imgTag = document.createElement("img").asInstanceOf[HTMLImageElement]
    imgTag.src = src
    imgTag
  }

  def preciseImageElement(width:Int, height:Int, id: String, src: String): HTMLImageElement = {
    val imgTag:HTMLImageElement = document.createElement("img").asInstanceOf[HTMLImageElement]
    imgTag.width = width
    imgTag.height = height
    imgTag.id = id
    imgTag.src = src
    imgTag
  }

  def toHtmlImage(canvas:Canvas): HTMLImageElement = preciseImageElement(canvas.width, canvas.height, canvas.id, canvas.toDataURL("image/png"))

  def toHtmlImage(img:Img): HTMLImageElement = toHtmlImage(toCanvas(img))

  def toHtmlImage(aImg: AsyncImg): Future[HTMLImageElement] = aImg.reserveImgData[HTMLImageElement]( img => toHtmlImage( img ) )

  def imageDataToImg(imageData: ImageData): Img = new Img(
    imageData.width,
    imageData.data
  )

  def imgToImageData(img: Img): ImageData = {
    val ctx = canvasElement(img.width, img.height).getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    val imageData = ctx.getImageData(0, 0, img.width, img.height)
    var i: Int = 0
    while (i < imageData.data.length) {
      imageData.data(i) = img.pixelData(i)
      i = i + 1
    }
    imageData
  }

  def canvasToImg(canvas:Canvas): Img = imageDataToImg(
    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D].getImageData(
      0, 0, canvas.width, canvas.height
    )
  )

  def htmlImageElementToCanvas(htmlImageElement: HTMLImageElement): Canvas = {
    val canvas: Canvas = canvasElement(htmlImageElement.naturalWidth, htmlImageElement.naturalHeight, s"Canvas${htmlImageElement.id}")

    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D].drawImage(
      htmlImageElement,
      0, 0, htmlImageElement.naturalWidth, htmlImageElement.naturalHeight,
      0, 0, htmlImageElement.naturalWidth, htmlImageElement.naturalHeight
    )
    canvas
  }

  def htmlImageElementToImg(htmlImageElement: HTMLImageElement): Img = canvasToImg(
    htmlImageElementToCanvas(htmlImageElement)
  )

  def htmlImageElementToAsyncImg(htmlImageElement: HTMLImageElement): AsyncImg = {
    val img = htmlImageElementToImg(htmlImageElement)
    new AsyncImg(new Img(img.width, img.pixelData))
  }

  def blankImageData(width: Int, height: Int): ImageData = {
    val data = new Uint8ClampedArray(width * height * 4)
    var i: Int = 3
    while (i < data.length) {
      data(i) = 0xff
      i = i + 4
    }
    new ImageData(data, width, height )
  }
}