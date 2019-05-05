# img
A cross published Scala.js library for Image Processing.

## Overview:
This Scala.js library brings convenient, high performance, image representation classes with low level processing capabilities to JavaScript and JVM languages.

## Internals:
From the perspective of Scala on the JVM, the Img class wraps the <a href="https://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferedImage.html">BufferedImage</a> class and has lightning fast implicit conversions between the two representations.  This reliance on BufferedImage maximizes interoperability with other JVM image processing libraries.

From the perspective of Scala.js or plain Javascript environments such as web browsers and Node.js, the Img class wraps the native <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Uint8ClampedArray">Uint8ClampedArray</a> to facilitate interoperability with Canvas, pixel data transfer to WebWorkers, and fast data serialization.