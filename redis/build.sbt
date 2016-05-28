name := "redis"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % "1.5.2" % "provided",
  "org.apache.spark" % "spark-sql_2.10" % "1.5.2" % "provided",
  "org.apache.spark" % "spark-streaming_2.10" % "1.5.2" % "provided",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.5.2"
)

libraryDependencies += "redis.clients" % "jedis" % "2.8.0"

// new repo on maven.org
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.6.0"


// old repo on bintray (1.5.0 and inferior version)
resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"
libraryDependencies += "com.etaty.rediscala" %% "rediscala" % "1.5.0"