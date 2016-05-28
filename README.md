
Requirements
============
This pipeline requires Kafka 0.8+, Spark 1.5+, Redis 3.0.3, Scala 2.10.4

Running the streaming pipeline
=======================
Spark-Redis must be submitted with the `--jars` option:

    spark-submit --class StreamBin --executor-memory 6000M --driver-memory 6000M --jars ./target/scala-2.10/spark_stream-assembly-1.0.jar ./target/scala-2.10/spark_stream_2.10-1.0.jar ./spark-redis-0.1.1-jar-with-dependencies.jar

Running the batch processing script
======================
The file name with domain IDs is passed in, for example:

    spark-submit --packages com.databricks:spark-csv_2.10:1.4.0 translate.py domain.csv