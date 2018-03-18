//package ai.dragonfly.img
//
//import ai.dragonfly.color._
//import ai.dragonfly.math.stats.kernel.{DiscreteKernel, GaussianKernel}
//import ai.dragonfly.math.stats.{StreamingStats, StreamingVectorStats}
//import ai.dragonfly.math.vector.{Vector3, WeightedVector3}
//import ai.dragonfly.spacial.PointRegionOctree
//
//import scala.collection.mutable.ListBuffer
//
//// for color palette extraction
//
///*
//Technical goals, long term and short:
//
//Why am I doing MeanShift in Scala.js?
//Lighter versions of color tree.
//Novel features for Image Processing Library
//
//
//
//Why am I doing this?
//
//Color Palette Extraction uses a lot of memory and processor resources.  Using the browser cuts costs by distributing work to the clients.
//
// */
//
//object MeanShift {
//
//  // What is the output?
//
//  def apply(img: ImageBasics): Unit = {
//
//    val colorFrequency = ColorHistogram.fromImage(img).hist
//    val leafOctTree = new PointRegionOctree[Double]( 100.0, Vector3( 50.0, 0.0, 0.0 ) )
//
//    for ((c, w) <- colorFrequency) {
//      val lab: LAB = Color.toLab(RGBA(c)).discretize()
//      leafOctTree.insert( Vector3( lab.L, lab.a, lab.b ), w )
//    }
//
//    // Each level of the hierarchical clustering as an Octree?
//
//    var radius = 2.0
//    while ( radius < 50.0 ) {
//
//      val kernel = DiscreteKernel(GaussianKernel(radius))
//      val svs = new StreamingVectorStats(3)
//      val parents = new PointRegionOctree[Leaf]( 100.0, Vector3( 50.0, 0.0, 0.0 ) )
//
//      for ((v, o) <- leafOctTree) {
//        var shifted = v
//        var shiftSquared = Double.MaxValue
//        var itercount = 0
//        while (shiftSquared < 0.000001 && itercount < 100) {
//          leafOctTree.radialQuery(v, radius) match {
//            case Some(neighborhood) =>
//              for ((p: Vector3, w: Double) <- neighborhood) {
//                svs(p, kernel.weight(p.subtract(shifted)) * w)
//              }
//            case _ => itercount = 100
//          }
//          val avg = svs.average().asInstanceOf[Vector3]
//          shiftSquared = avg.distanceSquaredTo(shifted)
//          shifted = avg
//          itercount = itercount + 1
//          svs.reset()
//        }
//        // Build up the tree here.
//
//        parents.insert(Vector3, )
//      }
//      radius = radius * 2.0
//    }
//
//  }
//
//}
//
///*
//  L*a*b* color space extrema:
//    L: (-5.5999998E-8, 100.0)
//    a: (-86.18464, 98.25422)
//    b: (-107.86368, 94.48248)
//*/
//
//trait ClusterNode {
//  var parent: Option[ClusterNode] = None
//  def weightedVector: WeightedVector3
//  def children: ListBuffer[ClusterNode]
//}
//
//class Leaf(override val weightedVector: WeightedVector3) extends ClusterNode {
//  override def children: ListBuffer[ClusterNode] = ListBuffer[ClusterNode]()
//}
//
//class Meta(val stats: StreamingVectorStats, childNodes:ListBuffer[ClusterNode]) extends ClusterNode {
//
//  override def children: ListBuffer[ClusterNode] = childNodes
//
//  override def weightedVector = WeightedVector3(stats.s0, stats.average.asInstanceOf[Vector3])
//
//  def addChild(child: ClusterNode): Unit = {
//    val cwv = child.weightedVector
//    stats(cwv.v3, cwv.weight)
//    children += child
//    child.parent = Some(this) // assign parent to child
//  }
//}
//
//class HierarchicalClustering( val root: ClusterNode )