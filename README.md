# img
A Scala.js Image Processing Library

This cross published Scala.js library brings convenient image processing capabilities to your application whether it runs in the browser, Node.js, or on the JVM.

Img, the name of this library, and it's core data type for representing digital images, pays homage to the <img src="..." /> tag in HTML.

From the perspective of Scala on the JVM, the Img class wraps the BufferedImage class and has lightning fast implicit conversions between the two representations.

From the perspective of Scala.js or plain Javascript in Javascript environments such as web browsers and Node.js, the Img class wraps the native Uint8ClampedArray to facilitate interoperability with Canvas, pixel data transfer to WebWorkers, and fast data serialization.

From the perspective of pure Javascript, Img behaves like any other javascript class.

When running in the browser, image operations complete asynchronously in a separate WebWorker.  To avoid the high memory costs involved with copying image data from the main thread to the worker thread, this library serializes method parameters into binary messages and transfers the image data via the Transferable features available to most WebWorker implementations.  This design choice dramatically improves performance but at a cost; code in the main thread can not access image data when the worker has access to it.  From scala.js, the library makes the results of asynchronous image operations available via futures while javascript code can associate the results with callback functions.