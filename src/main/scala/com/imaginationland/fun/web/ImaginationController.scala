package com.imaginationland.fun.web

import org.scalatra.{BadRequest, Ok, RequestEntityTooLarge, ScalatraServlet}
import org.scalatra.servlet.{SizeConstraintExceededException, FileItem, MultipartConfig, FileUploadSupport}

import scala.xml.Node

/**
 * Created by suryasev on 9/15/15.
 */
class ImaginationController extends ScalatraServlet with FileUploadSupport {

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(3*1024*1024),
                                             maxRequestSize = Some(3*1024*1024),
                                             fileSizeThreshold = Some(1*1024*1024)))

  def displayPage(content: Seq[Node]) = Template.page("File upload example", content, url(_))

  def processFile(fileUpload: FileItem) = {

  }

//  error {
//    case e: SizeConstraintExceededException => RequestEntityTooLarge("The maximum file size accepted is 3 MB!")
//  }

  get("/") {
    displayPage(
      <form action={url("/upload")} method="post" enctype="multipart/form-data">
        <p>File to upload: <input type="file" name="file" /></p>
        <p><input type="submit" value="Upload" /></p>
      </form>
        <p>
          Upload a file using the above form. After you hit "Upload"
          the file will be uploaded and your browser will start
          downloading it.
        </p>

        <p>
          The maximum file size accepted is 3 MB.
        </p>)
  }

  post("/upload") {
    fileParams.get("file") match {
      case Some(file) =>
        Ok(file.get(), Map(
          "Content-Type"        -> (file.contentType.getOrElse("application/octet-stream")),
          "Content-Disposition" -> ("attachment; filename=\"" + file.name + "\"")
        ))  //processFile(fileParams.get("thefile"))

      case None =>
        BadRequest(displayPage(
          <p>
            Hey! You forgot to select a file.
          </p>))
    }
  }
}
