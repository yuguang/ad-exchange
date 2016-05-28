import sbt._

object MyBuild extends Build {
  lazy val root = Project("root", file(".")) dependsOn(redisLab)
  lazy val redisLab = RootProject( uri("git://github.com/RedisLabs/spark-redis.git") )
}