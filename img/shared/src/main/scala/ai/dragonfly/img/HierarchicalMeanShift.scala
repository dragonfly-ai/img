package ai.dragonfly.img

import ai.dragonfly.math.stats.kernel.{GaussianKernel, Kernel}
import ai.dragonfly.math.stats.{StreamingStats, StreamingVectorStats}
import ai.dragonfly.math.vector.{Vector3, WeightedVector3}
import ai.dragonfly.spacial._

import scala.collection.mutable

/**
  * Created by clifton on 5/15/15.
  */

trait ClusterNode {
  def getFrequency: Double = weightedVector.weight

  private var parent: Meta = _ // this evaluates as null?

  def setParent(p: Meta): ClusterNode = {
    this.parent = p
    this
  }
  def weightedVector: WeightedVector3
  def children: Option[mutable.HashSet[ClusterNode]]
  def hasParent: Boolean = parent != null
  def getParent() = parent
}

case class Leaf(override val weightedVector: WeightedVector3) extends ClusterNode {
  override def children = None
}

object Meta {
  def apply(): Meta = new Meta(new StreamingVectorStats(3))
}

class Meta(val stats: StreamingVectorStats, childNodes:mutable.HashSet[ClusterNode] = new mutable.HashSet[ClusterNode]()) extends ClusterNode {

  var distSquaredStats = new StreamingStats  // can this be done with distance squared?

  override def children = if (childNodes.size <= 0) None else Some(childNodes)

  override def weightedVector = {
    WeightedVector3(stats.s0, stats.average.asInstanceOf[Vector3])
  }

  def addChild(child: ClusterNode): Unit = {
    stats(child.weightedVector.v3)

    for (cn <- childNodes.iterator) distSquaredStats(cn.weightedVector.v3.distanceSquaredTo(child.weightedVector.v3))

    childNodes.add(child)
    //this.weightedVector.increaseFrequency(child.weightedVector.weight)
    child.setParent(this) // assign parent to child
  }
}

object HierarchicalMeanShift {

  def defaultSnap(r: Double)(v: Vector3) = {
    v.divide(r)
    v.round()
    v.scale(r)
  }

  def weightedAverage(neighborhood: mutable.MutableList[(Vector3, ClusterNode)]): Vector3 = {
    val svs = new StreamingVectorStats(3)
    for ((_, cn) <- neighborhood) svs(cn.weightedVector.v3, cn.weightedVector.weight)
    svs.average().asInstanceOf[Vector3]
  }

  def weightedKernelAverage(center: Vector3, neighborhood: mutable.MutableList[(Vector3, ClusterNode)], k: Kernel): Vector3 = {
    val svs = new StreamingVectorStats(3)
    for ((_, cn) <- neighborhood) svs(cn.weightedVector.v3, k.weight(cn.weightedVector.v3, center) * cn.weightedVector.weight)
    svs.average().asInstanceOf[Vector3]
  }

  def shift(v: Vector3, prOctree: PointRegionOctree[ClusterNode], radius: Double, extendedRadius: Double, convergenceSensitivitySquared: Double = 0.0001, k: Kernel): Vector3 = {

    val erDiffSquared = Math.pow(extendedRadius - radius, 2)
    var distanceSquared = Double.MaxValue
    var count: Int = 0
    var currentVector = v
    var currentCenter = v

    try {
      var neighborhood = prOctree.radialQuery(v, extendedRadius)

      while (distanceSquared > convergenceSensitivitySquared && count < 100) {
        val shiftedVector = weightedKernelAverage(currentVector, neighborhood, k)
        distanceSquared = currentVector.distanceSquaredTo(shiftedVector)

        if (prOctree.size > 99 && shiftedVector.distanceSquaredTo(currentCenter) > erDiffSquared) {
          neighborhood = prOctree.radialQuery(shiftedVector, extendedRadius)
          currentCenter = shiftedVector
        }

        currentVector = shiftedVector
        count += 1
      }
    } catch {
      case e: Throwable => //e.printStackTrace()
    }
    currentVector
  }

  def cluster(prOctree: PointRegionOctree[ClusterNode], radius: Double, convergenceSensitivity: Double, snap: (Vector3) => Unit): PointRegionOctree[ClusterNode] = {

    println(s"clustering r = $radius")
    val parents = new PointRegionOctree[ClusterNode](150, Vector3(100.0,0.0,0.0))
    val extendedRadius = radius * 1.5
    val k = GaussianKernel(radius) // EpanechnikovKernel(extendedRadius) // UniformKernel(currentRadius)

    println(s"kernel: $k")

    var progressCount = 0.0
//    println(s"prOctree ${prOctree.map.values}")

    for (cn <- prOctree.map.values) {
      //println(s"${cn.weightedVector.v3}")
      progressCount = progressCount + 1.0

      val shiftedVector = shift(cn.weightedVector.v3, prOctree, radius, extendedRadius, convergenceSensitivity, k).round().asInstanceOf[Vector3]

      //print(s"${cn.weightedVector.v3} -> $shiftedVector")
/*
      if (progressCount % 500 == 0) {
        print(parents.size); println((" %3.2f" format (100.0 * progressCount / prOctree.size)) + " %")
      }
*/
      //snap(shiftedVector)

      if (shiftedVector == null) {
        println("shiftedVector is null")
      } else {
        val parent = parents.map.get(shiftedVector) match {
          case None =>
            val p = Meta()
            parents.insert(shiftedVector, p)
            p
          case Some(pn) => pn.asInstanceOf[Meta]
        }
        parent.addChild(cn)
      }
    }
    parents
  }
/*
  def refineCluster(parent: Meta,
                    convergenceSensitivity: Double,
                    snap: (Vector3) => Unit,
                    iteration: Int = 1,
                    scale: Int = 1
                   ): Meta = {

    val stats = parent.distSquaredStats
    val newRadius = stats.min //+ (stats.average - stats.min) / 4.0
    val adjustedParent = Meta()

    parent.children match {
      case Some(children) =>
        val middleMen = cluster(children, newRadius, convergenceSensitivity, snap)
        if (middleMen.size == 1) {
          println("FAIL big " + children.size + "->" + middleMen.size + " i=" + iteration + " r=" + newRadius)
          for ((_, cn) <- middleMen) adjustedParent.addChild(cn)
        } else if (middleMen.size == children.size) { // no useful consolidations
          println("FAIL small " + children.size + "->" + middleMen.size + " i=" + iteration + " r=" + newRadius)
          for ((_, cn) <- middleMen) adjustedParent.addChild(cn)
        } else {
          for ((_, cn) <- middleMen) {
            adjustedParent.addChild(
              if ( cn.children.size > 10 ) refineCluster(cn.asInstanceOf[Meta], convergenceSensitivity, snap, iteration + 1)
              else cn
            )
          }
        }
      case None =>
    }
    adjustedParent
  }
*/
  def meanShift(source: PointRegionOctree[ClusterNode],
                radius: Double = 2.0,
                step: (Double) => Double = (r: Double) => r * 1.5,
                convergenceSensitivity: Double = 0.0001,
                snap: (Vector3) => Unit = (v3: Vector3) => v3.round()): ClusterNode = {

    var orphans: PointRegionOctree[ClusterNode] = source
    var previousRadius = radius / 2.0
    var currentRadius = radius
    var iteration = 0

    while (orphans.size > 6 && currentRadius < 180) {
      iteration = iteration + 1
      println("Level: " + iteration + " R=" + currentRadius)

      val parents = cluster(orphans, currentRadius, convergenceSensitivity, snap)

      if (orphans.size == parents.size) {
        println("No parents found at radius: " + currentRadius)
      } else {
        println("found " + parents.size + " parents.")
        orphans = parents
      }

      //////
      previousRadius = currentRadius
      //currentRadius = step(currentRadius)

//      val analyticalParent = Meta()
//      for ((_, p) <- parents) analyticalParent.addChild(p)
//      val stats = analyticalParent.distSquaredStats
//      println(stats)

      //currentRadius = (stats.min + stats.average / 4.0)
      //println("stdvSquared " + analyticalParent.stats.standardDeviation)
      //currentRadius = (stats.average - analyticalParent.stats.standardDeviation)
      //currentRadius = Math.max(stats.min, stats.average / 10)
      currentRadius = currentRadius * 1.25 //Math.min(currentRadius + Math.sqrt(stats.min), currentRadius + 0.1 * currentRadius)
    }

    if (orphans.size > 1) {
      val root = Meta()
      for (cn <- orphans.map.values) {
        root.addChild(cn)
      }
      root
    } else orphans.iterator.next()._2

  }

}