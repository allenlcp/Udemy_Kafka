## Start zookeeper
zookeeper-server-start.sh ./config/zookeeper.properties

## Start kafka broker
kafka-server-start.sh config/server.properties


## Create topics
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --create --partitions 6 --replication-factor 1

--replication-factor cannot be greater than number of brokers

## List of topics 
kafka-topics.sh --zookeeper 127.0.0.1:2181 --list

## Topic details
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --describe

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --describe

## Delete topics
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --delete

## Producer (default props)
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic

## Producer (custom props)
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all

## Producer (write to non existing topic -> will be created on the fly)
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic new_topic

kafka-topics.sh --zookeeper 127.0.0.1:2181 --list

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic new_topic --describe
Creates topic with Partition qty of 1 and Replication factor qty of 1

Best practice - always create topic beforehand

However we can also update default config in "server.properties" file
``` bash
...
# A comma separated list of directories under which to store log files
log.dirs=/Users/mopom/kafka/kafka_2.12-2.0.0/data/kafka

# The default number of log partitions per topic. More partitions allow greater
# parallelism for consumption, but this will also result in more files across
# the brokers.
num.partitions=3

# The number of threads per data directory to be used for log recovery at startup and flushing at shutdown.
...
```

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic new_topic_2 --describe
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic new_topic_2

## Producer with keys
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
> key,value
> another key,another value


## Consumer
Command only intercepts msg as from launch - won't retrieve missed msg
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic 

## Consumer (read from beginning)
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning

## Consumer Same Group
Console 1
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application
Console 2
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application
Console 3
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application

## Consumer Different Group
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-application --from-beginning

## Groups
kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list
When use consumer and don't specify groups -> it generates a random one

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-second-application

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-first-application

## Resetting Offsets
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --to-earliest --execute --topic first_topic

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --shift-by -2 --execute --topic first_topic

Note -> use negative to move back; also does it for each partition (if 3 partition x 2 => consumer will receive 6 msg)

## Consumer with keys
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,

___

kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-third-application