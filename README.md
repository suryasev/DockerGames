How to build:
sbt docker

This app needs a Mongo instance.

To start a mongo instance with docker and then get its IP address do:
docker run -p 27017:27017 -i mongo:latest
docker ps
docker inspect <container_id> | grep IPAddress | cut -d '"' -f 4

T
docker run -e MONGO_HOST=<mongo_ip> -p 8080:8080 com.imaginationlanfun/image-matcher:v0.1

Navigate to:
<docker_ip>/imagination/
Then just upload images

Technology overview:
Used Scalatra MongoDB docker seed from typesafe
Scalatra for webapp
MongoDB + casbah + gridfs for database
Images traversed with (built-in) Java ImageIO
Images are compared by distance between average RGBA vectors (sum(red) / pixelCount; sum(blue) / pixelCount; etc.)
KDTrees used for log(n) lookups

Shortcuts taken:
Did not use static schemas (I usually prefer Avro encoding)
Testing very simple; ideally would also test scalatra, mongodb and so forth
Webapp templating not really done
Reindexing currently happens after every PUT; proper solution would do this with a background process instead
Actual file size limit can probably be way higher; need to investigate how JVM settings work w/ docker
Edge cases not really considered (e.g. not checking that something is not an image)
Images are unique by name; this can be done more cleverly
RGBAVectors were meant to be Semigroups