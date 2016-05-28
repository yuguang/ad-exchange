import redis, os
from pyspark.sql import SQLContext
from pyspark import SparkContext, SparkConf
from pyspark.sql.types import *
from pyspark.sql.functions import *
import argparse


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("file", help="A CSV file with domain, domainId entries")
    args = parser.parse_args()
    conf = SparkConf().setAppName("csv-reader")
    sc = SparkContext(conf=conf)
    sqlContext = SQLContext(sc)
    fields = [StructField("domainId", StringType(), True),
              StructField("domain", StringType(), True),]

    df = sqlContext.read \
        .format('com.databricks.spark.csv') \
        .options(header='true') \
        .load(args.file, schema=StructType(fields))

    def translate(entry):
        r = redis.StrictRedis(host=os.environ.get('REDIS_HOST'), port=os.environ.get('REDIS_PORT'))
        return r.get(entry.domainId + ',' + entry.domain)

    df.map(translate).write.format('com.databricks.spark.csv').save('exchange.csv')
