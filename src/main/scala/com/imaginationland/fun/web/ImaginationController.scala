package com.imaginationland.fun.web

import java.awt.Color
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import com.imaginationland.fun.data._
import com.thesamet.spatial.KDTree
import org.scalatra.servlet.{FileItem, FileUploadSupport, SizeConstraintExceededException}
import org.scalatra.{BadRequest, Ok, RequestEntityTooLarge, ScalatraServlet}

import scala.xml.Node


/**
 * Created by suryasev on 9/15/15.
 */
class ImaginationController extends ScalatraServlet with FileUploadSupport with DataModule with ImageRepresentationOrdering {

  var tree = KDTree.fromSeq(Seq[ImageRepresentation]())

  /**
   * Simple templating (from docker seed)
   *
   * @param content
   * @return
   */
  def displayPage(content: Seq[Node]) = Template.page("Similar Images", content, url(_))

  /**
   * Store file data to Mongo; compute RGBA Vectors and store them to mongo
   *
   * @param fileUpload
   */
  def processFile(fileUpload: FileItem) = {
    val imageByteArray = fileUpload.get()

    val image = ImageIO.read(new ByteArrayInputStream(imageByteArray))

    //Loop through every pixel and covert it to an ImageIO RGBA Color object
    val rgbArray = new Array[Int](image.getWidth * image.getHeight)
    val pixels = image.getRGB(0, 0, image.getWidth, image.getHeight, rgbArray, 0, 1).toList.map(new Color(_, true))

    //Take the average of all R, all B, etc. across all pixels in the image
    val imageVector = pixels.map(c => new RGBAVector(c.getRed, c.getGreen, c.getBlue, c.getAlpha))
      .reduce(_ append _).divide(pixels.size)

    //Save the image for later!
    mongoPutImageRepresentation(new ImageRepresentation(fileUpload.name, imageVector))
    mongoPutFile(fileUpload.name, imageByteArray)

  }

  /**
   * Grab n+1 most similar images (since the most similar image will be this image itself if we re-indexed)
   */
  def fetchSimilarImages(ir: ImageRepresentation, n: Int) = {
    tree.findNearest(ir, n + 1).filter(_.fileName != ir.fileName).take(n)
  }

  //Should be called in a background process
  def reindex =
    tree = KDTree.fromSeq[ImageRepresentation](mongoGetAllImageRepresentations.toSeq)

  error {
    case e: SizeConstraintExceededException => RequestEntityTooLarge("The maximum file size accepted is 1 MB!")
  }

  get("/") {
    displayPage(
      <form action={url("/upload")} method="post" enctype="multipart/form-data">
        <p>File to upload:
          <input type="file" name="file"/>
        </p>
        <p>
          <input type="submit" value="Upload"/>
        </p>
      </form>
        <p>
          Upload a file using the above form. After you hit "Upload"
          the file will be uploaded.
        </p>

        <p>
          The maximum file size accepted is 1 MB.
        </p>)
  }

  get("/reindex") {
    reindex
    Ok(displayPage(<p>Reindexing done!
      {mongoGetAllImageRepresentations.toList.toString}
    </p>))
  }

  //Currently processFile and reindex runs on upload; in a prod env, both of these should run as part of a background job processor
  post("/upload") {
    fileParams.get("file") match {
      case Some(file) if file.size > 0 => {
        processFile(file)
        reindex
        redirect(s"/imagination/overactive/${file.name}")
      }
      case _ =>
        BadRequest(displayPage(
          <p>
            Hey! You forgot to select a file.
          </p>))
    }
  }

  //Display an image
  get("/img/:image_name") {
    mongoGetFile(params("image_name")) match {
      case Some(image: Array[Byte]) => {
        contentType = "image/png"
        response.getOutputStream.write(image)
      }
      case None =>
        BadRequest(displayPage(
          <p>
            Hey! This file does not exist
          </p>))
    }
  }

  get("/overactive/:image_name") {
    mongoGetImageRepresentation(params("image_name")) match {
      case Some(image: ImageRepresentation) => {
        val similarImages = fetchSimilarImages(image, 3)
        Ok(displayPage(
          <h3>
            Primary Imagination
          </h3>
              <img src={"/imagination/img/" + image.fileName}/>
            <h3>
              Similar Images
            </h3>
            <div>
              {similarImages.map(si =>
                <img src={"/imagination/img/" + si.fileName}/>
            )}
            </div>
            <h4>Upload another?</h4>
            <form action={url("/upload")} method="post" enctype="multipart/form-data">
              <p>File to upload:
                <input type="file" name="file"/>
              </p>
              <p>
                <input type="submit" value="Upload"/>
              </p>
            </form>
        ))
      }
      case None =>
        BadRequest(displayPage(
          <p>
            Hey! This image does not exist.
          </p>))
    }
  }
}
