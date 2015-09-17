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
import org.slf4j.{Logger, LoggerFactory}

object ImaginationProvider extends App with WebModule with DataModule {
  val logger = LoggerFactory.getLogger(getClass)
  val config = ConfigFactory.load()
  val server = new Server(config.getInt("http.port"))
  val webCtx = new WebAppContext()
  webCtx.setContextPath(config.getString("http.path"))
  webCtx.setResourceBase("/WEB-INF")
  webCtx.addServlet(new ServletHolder(imaginationController), "/imagination/*")

  server.setHandler(webCtx)
  server.start
  logger.info("Server started.")
  server.join
}