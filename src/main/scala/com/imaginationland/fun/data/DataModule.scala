package com.imaginationland.fun.data

import java.io.ByteArrayOutputStream

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.gridfs.Imports._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

trait DataModule {

  val paramConfig: Config = ConfigFactory.load()

  lazy val mongo = {
    val host = paramConfig.getString("mongo.host")
    val port = paramConfig.getInt("mongo.port")

    val logger = LoggerFactory.getLogger(classOf[DataModule])
    logger.info("Mongo client: host={} port={}", host, port)

    MongoClient(host, port)
  }

  lazy val db = mongo(paramConfig.getString("mongo.db"))

  db.dropDatabase()

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
      stream.toByteArray
    })
  }

  lazy val metadataCollection = db(paramConfig.getString("mongo.collections.metadata"))

  //Shortcut solution; this should use a static schema
  //Update if fileName exists; else insert
  def mongoPutImageRepresentation(image: ImageRepresentation) =
    metadataCollection.update(MongoDBObject("name" -> image.fileName), image.toMongoDBObject, true)

  def mongoGetImageRepresentation(key: String) =
    metadataCollection.findOne(MongoDBObject("name" -> key)).map(ImageRepresentation.fromDBObject(_))

  def mongoGetAllImageRepresentations =
    metadataCollection.find().map(ImageRepresentation.fromDBObject(_))

}
