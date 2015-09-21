package com.imaginationland.fun.data

import java.io.ByteArrayOutputStream

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.gridfs.Imports._
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

trait DataModule {

  val config: Config

  lazy val mongo = {
    val host = config.getString("mongo.host")
    val port = config.getInt("mongo.port")

    val logger = LoggerFactory.getLogger(classOf[DataModule])
    logger.info("Mongo client: host={} port={}", host, port)

    MongoClient(host, port)
  }

  lazy val db = mongo(config.getString("mongo.db"))

  //GridFS is a solution for storing large files or byte arrays
  lazy val fileGridFs = GridFS(db)

  /**
   * Store a byte array to mongodb
   * @param key
   * @param fileData
   */
  def mongoPutFile(key: String, fileData: Array[Byte]) = {
    val f = fileGridFs.createFile(fileData)
    f.filename_=(key)
    f.save()
  }

  /**
   * Read a byte array from mongodb
   * @param key
   * @return Option of bytearray keyed by key
   */
  def mongoGetFile(key: String) = {
    val f = fileGridFs.findOne(key)
    f.map({ contents =>
      val stream = new ByteArrayOutputStream()
      contents.writeTo(stream)
      stream.toByteArray()
    })
  }

  lazy val metadataCollection = db(config.getString("mongo.collections.metadata"))

  //  lazy val locationCollection = db(config.getString("mongo.collections.locations"))
  //  lazy val locationRepository: LocationRepository = wire[MongoLocationRepository]

}
