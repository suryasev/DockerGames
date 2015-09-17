import AssemblyKeys._
import DockerKeys._
import sbtdocker.mutable.Dockerfile
import sbtdocker.ImageName

name := "scalatra-mongodb-seed"

organization := "com.imaginationland.fun"

version := "0.1"

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
    "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= {
    val configVersion     = "1.2.1"
    val logbackVersion    = "1.1.1"
    val scalatraVersion   = "2.4.0.M3"
    val jettyVersion      = "9.3.0.RC0"
    val json4sVersion     = "3.2.9"
    val casbahVersion     = "2.8.0"
    val salatVersion      = "1.9.9"
    val embedMongoVersion = "0.2.2"
    val macwireVersion    = "0.8.0"
    val scalaMockVersion  = "3.2.1"
    Seq(
        "com.typesafe"              %   "config"                      % configVersion,
        "org.scalatra"              %%  "scalatra"                    % scalatraVersion,
        "org.scalatra"              %%  "scalatra-json"               % scalatraVersion,
        "org.scalatra"              %%  "scalatra-scalatest"          % scalatraVersion % "test",
        "org.json4s"                %%  "json4s-jackson"              % json4sVersion,
        "org.eclipse.jetty"         %   "jetty-server"                % jettyVersion,
        "org.eclipse.jetty"         %   "jetty-webapp"                % jettyVersion,
        "org.mongodb"               %%  "casbah-core"                 % casbahVersion,
        "com.novus"                 %%  "salat"                       % salatVersion,
        "com.github.simplyscala"    %%  "scalatest-embedmongo"        % embedMongoVersion % "test",
        "com.softwaremill.macwire"  %%  "macros"                      % macwireVersion,
        "com.softwaremill.macwire"  %%  "runtime"                     % macwireVersion,
        "ch.qos.logback"            %   "logback-classic"             % logbackVersion,
        "org.scalamock"             %%  "scalamock-scalatest-support" % scalaMockVersion % "test"
    )
}

mainClass := Some("com.imaginationland.fun.ImaginationProvider")

initialCommands in console := """
    import collection.JavaConversions._
"""

Revolver.settings

assemblySettings

jarName in assembly := "imagination-provider.jar"

dockerSettings

docker <<= (docker dependsOn assembly)

dockerfile in docker := {
    val artifact = (outputPath in assembly).value
    val artifactTargetPath = s"/app/${artifact.name}"
    val configFile = baseDirectory.value / "src" / "main" / "resources" / "docker.conf"
    val configFileTargetPath = s"/app/application.conf"
    val logbackFile = baseDirectory.value / "src" / "main" / "resources" / "logback.xml"
    val logbackFileTargetPath = "/app/logback.xml"
    new Dockerfile {
        from("java:8")
        add(artifact, artifactTargetPath)
        add(configFile, configFileTargetPath)
        add(logbackFile, logbackFileTargetPath)
        env("MONGO_DB",   "test")
        env("MONGO_HOST", "mongodb.example.com")
        env("MONGO_PORT", "27017")
        entryPoint("java",
                  s"-Dconfig.file=${configFileTargetPath}",
                  s"-Dlogback.configurationFile=${logbackFileTargetPath}",
                   "-jar", artifactTargetPath)
    }
}

imageName in docker := {
    ImageName(namespace = Some(organization.value),
              repository = name.value,
              tag = Some("v" + version.value))
}
