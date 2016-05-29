
Requirements
============
This pipeline requires Kafka 0.8+, Spark 1.5+, Redis 3.0.3, Scala 2.10.4

Compile sbt project
===================
(Spark-Redis)[https://github.com/RedisLabs/spark-redis] is one of the dependencies of the Spark streaming application.
It's not an official package yet, so I am building the project against the source.

    git clone https://github.com/yuguang/ad-exchange
    git clone https://github.com/RedisLabs/spark-redis
    cp -afr spark-redis/src/main/scala/* ad-exchange/redis/src/main/scala
    sbt assembly
    sbt package

You should now find `ad-stream-assembly-1.0.jar` in the `ad-exchange/redis/target/scala-2.10/` folder.

Build and run app with docker compose
=====================================
To simplify the installation and configuration of required software, I used Docker images.
First, you need to clone this repo and change into the Docker project directory

    git clone https://github.com/yuguang/ad-exchange
    cd ad-exchange/docker
    docker-compose up

Enter `http://0.0.0.0:5000/` in a browser to see the application running.
Next, we will create a topic in Kafka:

    ./start-kafka-shell.sh localhost localhost:9092
    $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper $ZOOKEEPER --replication-factor 1 --partitions 2 --topic rawdata

Note that you should change the repication factor and number of partitions in production.
I use these numbers simply for demonstration purposes.
Third, we run the Spark streaming application

    spark-submit \
    --master yarn-client \
    app/ad-stream-assembly-1.0.jar kafka:9092

Finally run the following in the `kafka` contianer:

    $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list $KAFKA --topic rawdata

Running the batch processing script
======================
The file name with domain IDs is passed in, for example:

    spark-submit --packages com.databricks:spark-csv_2.10:1.4.0 translate.py domain.csv