# img
A Scala.js Image Processing Library

This cross published Scala.js library brings convenient image processing capabilities to your application whether it runs in the browser, Node.js, or on the JVM.

Img, the name of this library, and it's core data type for representing digital images, pays homage to the <img src="..." /> tag in HTML.

From the perspective of Scala on the JVM, the Img class wraps the BufferedImage class and has lightning fast implicit conversions between the two representations.

From the perspective of Scala.js or plain Javascript in Javascript environments such as web browsers and Node.js, the Img class wraps the native Uint8ClampedArray to facilitate interoperability with Canvas, pixel data transfer to WebWorkers, and fast data serialization.

From the perspective of pure Javascript, Img behaves like any other javascript class.

When running in the browser,
