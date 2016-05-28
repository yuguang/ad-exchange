/**
  * Created by yuguang on 5/27/16.
  */
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import com.redislabs.provider.redis._
import scala.util.Random
import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object StreamBin {
  private val batchInterval = getEnv("BATCH_INTERVAL", "2").toInt
  private val checkpointURL = getEnv("CHECKPOINT_URL", "hdfs://localhost:9000/checkpoint")
  private val zkQuorum = getEnv("ZOOKEEPER_QUORUM", "localhost:2181")
  private val brokers = getEnv("KAFKA_BROKER", "ec2-52-88-49-174.us-west-2.compute.amazonaws.com:9092")
  private val topics = getEnv("KAFKA_TOPIC", "rawdata")
  private val redisHost = getEnv("REDIS_HOST", "localhost")
  private val redisPass = getEnv("REDIS_PASS", "1pass")
  private val redisPort = getEnv("REDIS_PORT", "6379").toInt
  // set the expire time to 7 days
  private val expireTime = getEnv("EXPIRES", (7 * 24 * 60 * 60).toString).toInt

  def main(args: Array[String]) {
    val topicsSet = topics.split(",").toSet

    val redisClient = RedisClient(redisHost, redisPort, Option(redisPass))

    val sc = new SparkContext(new SparkConf()
      .setMaster("local").setAppName(getClass.getName)
      .set("redis.host", redisHost)
      .set("redis.port", redisPort.toString)
      .set("redis.auth", redisPass))
    val ssc = new StreamingContext(sc, Seconds(batchInterval))

    ssc.checkpoint(checkpointURL)

    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    // Tokenize messages. Persist the stream
    val visitorStream = kafkaStream.map(pair => {
      val (_, str) = pair
      val splits = str.stripLineEnd.split(",")
      ((splits(0), splits(1), splits(2)), (splits(0), splits(1), splits(2)))
    }).persist()

    visitorStream.foreachRDD(rdd => {
      // reduce by key so we will only get one record for every primary key
      val reducedRDD =
        rdd.reduceByKey((a,b) => if (a._1.compareTo(b._1) > 0) a else b)
      reducedRDD.map(r => {
        // fill in the exchangeId if not present
        var exchangeId = r._1._1
        if (exchangeId.length() == 0) {
          exchangeId = Random.nextInt(1000000).toString
        }
        r._1._2 + "," + r._1._3, r._1._1

      })
    })

    sc.toRedisHASH(exchangeId, f"$domain%s$domainId%d", expireTime)
  }

  def getEnv(name: String, default: String) = {
    sys.env.get(name).getOrElse(default)
  }
}

