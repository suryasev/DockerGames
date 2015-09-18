package com.imaginationland.fun

/**
 * Created by suryasev on 9/15/15.
 */

import com.imaginationland.fun.data.DataModule
import com.imaginationland.fun.web.WebModule
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.MultipartConfig
import org.slf4j.{Logger, LoggerFactory}

object ImaginationProvider extends App with WebModule with DataModule {
  val logger = LoggerFactory.getLogger(getClass)
  val config = ConfigFactory.load()
  val server = new Server(config.getInt("http.port"))
  val webCtx = new WebAppContext()
  webCtx.setContextPath(config.getString("http.path"))
  webCtx.setResourceBase("/WEB-INF")
  val holder = new ServletHolder(imaginationController)

  //Something buggy about jetty + normal way of setting multipartconfig
  holder.getRegistration.setMultipartConfig(
    MultipartConfig(
      maxFileSize = Some(3*1024*1024),
      fileSizeThreshold = Some(1*1024*1024)
    ).toMultipartConfigElement
  )

  webCtx.addServlet(holder, "/imagination/*")

  server.setHandler(webCtx)
  server.start
  logger.info("Server started.")
  server.join
}