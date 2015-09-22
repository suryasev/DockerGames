How to build:
sbt docker

The app needs a mongo instance.  To start a mongo instance with docker and then pass in its IP address do:
docker run -p 27017:27017 -i mongo:latest
docker ps
docker inspect <container_id> | grep IPAddress | cut -d '"' -f 4

Then:
docker run -e MONGO_HOST=<mongo_ip> -p 8080:8080 suryasev/image-matcher:v0.1

Navigate to:
<docker_ip>:8080/imagination/
Then just upload images
