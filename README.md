# img
A cross published Scala.js library for Image Processing.

## Overview:
This Scala.js library brings convenient, high performance, image processing capabilities to your application whether it runs in the browser, Node.js, or on the JVM.  Users can use this library natively from javascript, Scala, and other JVM languages.

## Capabilities:
<table>
  <tr><td>Name</td><td>Signature</td><td>Description</td></tr>
  <tr>
    <td>Unsharpen Mask</td>
    <td>def unsharpenMaskRGB(img: ImageBasics, radius: Int, amount: Double, threshold: Int = 0): ImageBasics</td>
    <td>Sharpen an image.</td>
  </tr>
  <tr>
    <td>Randomize RGB</td>
    <td>def randomizeRGB(img: ImageBasics): ImageBasics</td>
    <td>Generates an image of random pixels sampled from RGB color space.</td>
  </tr>
  <tr>
    <td>Flip Horizontal</td>
    <td>def flipHorizontal(img: ImageBasics): ImageBasics</td>
    <td>Flip an image about its vertical axis.</td>
  </tr>
  <tr>
    <td>Flip Vertical</td>
    <td>def flipVertical(img: ImageBasics): ImageBasics</td>
    <td>Flip an image about its horizontal axis.</td>
  </tr>
  <tr>
    <td>Rotate 90 Degrees</td>
    <td>def rotate90Degrees(img: ImageBasics, counterClockwise: Boolean = false): ImageBasics</td>
    <td>Rotate an image 90 degrees clockwise.</td>
  </tr>
  <tr>
    <td>Rotate 180 Degrees</td>
    <td>def rotate180Degrees (img: ImageBasics): ImageBasics</td>
    <td>Rotate an image 180 degrees.</td>
  </tr>
  <tr>
    <td>Rotate Degrees</td>
    <td>def rotateDegrees(img: ImageBasics, angleDegrees: Double): ImageBasics</td>
    <td>Rotate an image clockwise by an angle specified in degrees.  Relies on bilinear interpolation.</td>
  </tr>
  <tr>
    <td>Rotate Radians</td>
    <td>def rotateRadians(img: ImageBasics, angleRadians: Double): ImageBasics</td>
    <td>Rotate an image clockwise by an angle specified in radians.  Relies on bilinear interpolation.</td>
  </tr>
  <tr>
    <td>Overlay</td>
    <td>def overlay(bgImg: ImageBasics, fgImg: ImageBasics, bgX: Int, bgY: Int, fgX: Int, fgY: Int, width: Int, height: Int): ImageBasics</td>
    <td>Blend an image with another based on alpha channels.</td>
  </tr>
  <tr>
    <td>Negative</td>
    <td>def negative(img: ImageBasics): ImageBasics</td>
    <td>Invert the red, green, and blue channels in RGB space.</td>
  </tr>
  <tr>
    <td>Grayscale Average RGB</td>
    <td>def grayscaleAverageRGB(img: ImageBasics): ImageBasics</td>
    <td>Discard color information from an image by collapsing the red, green, and blue values of each of its pixels into simple averages.</td>
  </tr>
  <tr>
    <td>Grayscale L*a*b* Intensity</td>
    <td>def grayscaleLABIntensity(img: ImageBasics): ImageBasics</td>
    <td>Discard color information from each pixel by converting it to L*a*b* space and replacing the a* and b* values with zeros.</td>
  </tr>
  <tr>
    <td>Equalize RGB</td>
    <td>def equalizeRGB(img: ImageBasics): ImageBasics</td>
    <td>Equalize an image's Histogram in RGB space.</td>
  </tr>
  <tr>
    <td>Median Cut</td>
    <td>def median(img: ImageBasics, radius: Int): ImageBasics</td>
    <td>Replace each pixel in an image with the median of all other pixels within a radius of the original pixel.</td>
  </tr>
  <tr>
    <td>Scale</td>
    <td>def scale(img: ImageBasics, newWidth: Int, newHeight: Int ): ImageBasics</td>
    <td>Adjust the dimensions of an image.  It supports uniform and non-uniform scaling and relies on bilinear interpolation.</td>
  </tr>
  <tr>
    <td>Threshold RGB</td>
    <td>def thresholdRGB(img: ImageBasics, intensity: Int): ImageBasics</td>
    <td>Map the color components of each pixel to either 0 or 255 depending on whether the given intensity is less than or greater than the threshold parameter.</td>
  </tr>
  <tr>
    <td>Threshold L*a*b*</td>
    <td>def thresholdLab(img: ImageBasics, intensityRGB: Int): ImageBasics</td>
    <td>Generates a binary image from a given image.  Each new pixel becomes white or black depending on whether each pixel intensity exceeds or falls short of the specified intensity.</td>
  </tr>
  <tr>
    <td>Brightness</td>
    <td>def brightness(img: ImageBasics, b: Int): ImageBasics</td>
    <td>Adjust the brightness of an image.</td>
  </tr>
  <tr>
    <td>Contrast</td>
    <td>def contrast(img: ImageBasics, c: Double): ImageBasics</td>
    <td>Adjust the contrast of an image.</td>
  </tr>
  <tr>
    <td>Uniform Blur RGB</td>
    <td>def uniformBlurRGB(toBlur: ImageBasics, radius: Int): ImageBasics</td>
    <td>Blur an image with a uniform kernel.</td>
  </tr>
  <tr>
    <td>Epanechnikov Blur RGB</td>
    <td>def epanechnikovBlurRGB(toBlur: ImageBasics, radius: Int): ImageBasics</td>
    <td>Blur an image with an Epanechnikov kernel.</td>
  </tr>
  <tr>
    <td>Gaussian Blur RGB</td>
    <td></td>
    <td>Blurs an image with a Gaussian kernel.</td>
  </tr>
  <tr>
    <td>Difference Matte</td>
    <td>def differenceMatte(img1: ImageBasics, img2: ImageBasics): ImageBasics</td>
    <td>Compute the difference between two images in RGB space.</td>
  </tr>
  <tr>
    <td></td>
    <td></td>
    <td></td>
  </tr>
</table>

## Internals:
From the perspective of Scala on the JVM, the Img class wraps the <a href="https://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferedImage.html">BufferedImage</a> class and has lightning fast implicit conversions between the two representations.  This reliance on BufferedImage maximizes interoperability with other JVM image processing libraries.

From the perspective of Scala.js or plain Javascript environments such as web browsers and Node.js, the Img class wraps the native <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Uint8ClampedArray">Uint8ClampedArray</a> to facilitate interoperability with Canvas, pixel data transfer to WebWorkers, and fast data serialization.

When running in the browser, image operations complete asynchronously in a separate WebWorker.  To avoid the high memory costs involved with copying image data from the main thread to the worker thread, this library serializes method parameters into binary messages and transfers the image data via the Transferable features available to most WebWorker implementations.  This design choice dramatically improves performance but at a cost; code in the main thread can not access image data when the worker has access to it.  From scala.js, the library makes the results of asynchronous image operations available via futures while javascript code can associate the results with callback functions.