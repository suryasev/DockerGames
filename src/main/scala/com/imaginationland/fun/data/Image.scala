package com.imaginationland.fun.data

import com.thesamet.spatial.{Metric, DimensionalOrdering}

/**
 * Created by suryasev on 9/17/15.
 */
/**
 *
 * @param red
 * @param green
 * @param blue
 * @param alpha
 */
case class RGBAVector(red: Float, green: Float, blue: Float, alpha: Float) {
  def append(b: RGBAVector): RGBAVector =
    new RGBAVector(red + b.red, green + b.green, blue + b.blue, alpha + b.alpha)

  /**
   * Convenience function for use with KBTree library
   *
   * @param dimension
   * @return
   */
  def byDimension(dimension: Int): Float = {
    dimension match {
      case 0 => red
      case 1 => green
      case 2 => blue
      case 3 => alpha
      case _ => throw new Exception("Attempting to get a dimension from RGBAVector that does not exist")
    }
  }

  /**
   * @return Tuple representation
   */
  def toTuple = (red, green, blue, alpha)

  def toSeq = Seq(red, green, blue, alpha)

  /**
   * Normalize RGBA by value n
   */
  def divide(n: Int) = new RGBAVector(red / n, green / n, blue / n, alpha / n)
}

class ImageRepresentation(val fileName: String, val vector: RGBAVector, val byteArray: Array[Byte])

class ImageDimensionalOrdering extends DimensionalOrdering[ImageRepresentation] {
  override def dimensions = 4

  //For some reason DimensionalOrdering requires an Int; faking a float comparison by *1000 and then round down
  override def compareProjection(dimension: Int)(x: ImageRepresentation, y: ImageRepresentation): Int =
    ((x.vector.byDimension(dimension) - y.vector.byDimension(dimension)) * 1000).toInt
}

class ImageDimensionalMetric extends Metric[ImageRepresentation, Float] {
  /** Returns the distance between two points. Modified from Metric codebase. */
  override def distance(x: ImageRepresentation, y: ImageRepresentation): Float =
    x.vector.toSeq.zip(y.vector.toSeq).map { z =>
      val d = z._1 - z._2
      d * d
    }.sum

  /** Returns the distance between x and a hyperplane that passes through y and perpendicular to
    * that dimension.
    */
  override def planarDistance(dimension: Int)(x: ImageRepresentation, y: ImageRepresentation): Float = {
    val dd = x.vector.byDimension(dimension) - y.vector.byDimension(dimension)
    dd * dd
  }
}

trait ImageRepresentationOrdering {
  //Implicit ImageRepresentation ordering for use by KDTree
  implicit val ord = new ImageDimensionalOrdering
  implicit val metric = new ImageDimensionalMetric
}