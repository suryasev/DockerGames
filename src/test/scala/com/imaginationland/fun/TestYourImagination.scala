package com.imaginationland.fun

import com.imaginationland.fun.data.{ImageRepresentationOrdering, RGBAVector, ImageRepresentation}
import com.thesamet.spatial.KDTree
import org.specs2.mutable.Specification

/**
 * Created by suryasev on 9/21/15.
 */
class TestYourImagination extends Specification with ImageRepresentationOrdering {
  "KDTreeTest" should {

    val first = new ImageRepresentation("first", RGBAVector(1,1,1,1))

    val someRepresentations =
      Seq(
        first,
        new ImageRepresentation("second", RGBAVector(1,6,6,1)),
        new ImageRepresentation("third", RGBAVector(1,1,2,1))
      )

    val tree = KDTree.fromSeq[ImageRepresentation](someRepresentations.toSeq)

    "get closest vector" in {
      val closest = tree.findNearest(first, 2).filter(_.fileName != first.fileName).head

      closest.fileName shouldEqual "third"
    }
  }
}
