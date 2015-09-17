package com.imaginationland.fun.web

import com.softwaremill.macwire.MacwireMacros._

trait WebModule {

//  def locationRepository: LocationRepository

  lazy val imaginationController = wire[ImaginationController]

}
