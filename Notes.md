<img width="500" alt="Topics, partitions and offsets" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0001.png">

## Topics
Is a particular stream of data
- Similar to a table in a database (without all the constraints)
- You can have as many topics as you want
- A topic is identified by its name


## Topics are split by Partitions
- Each partition is ordered
- Each message within a partition gets an incremental id, called offset


## Offsets
- Offset only have a meaning for a specific partition (e.g offset 3 in partition 0 doesn't represent the same data as offset 3 in partition 1)
- Order only guaranteed only within a partition (not across partitions)
- Data is kept only for a limited time (default in one week)
- Once the data is written to a partition, it can't be changed (immutability)
- Data is assigned randomly to a partition unless a key is provided 


## Brokers
- A Kafka clusters is composed of multiple brokers (servers)
- Each broker is identified with its ID (integer)
- Each broker contains certain topic partitions
- After connecting to any broker (Called a bootstrap broker), you will be connected to the entire cluster
- A good number to get started is 3 brokers, but some big clusters have over 100 brokers


## Brokers and topics
Example of Topic-A with 3 partitions and 100 brokers 
- Broker_101 will have Topic-A/Partition_0
- Broker_102 will have Topic-A/Partition_1
- Broker_103 will have Topic-A/Partition_3

Example of Topic-A with 2 partitions and 100 brokers 
- Broker_101 will have Topic-A/Partition_0
- Broker_102 will have Topic-A/Partition_1


## Replication
- Topic replication factor > 1 (usually between 2 and 3)
- This way if a broker is down, another broker can serve the data

Example: Topic-A with 2 partitions and replication factor of 2 (2 copies)
- Broker_101 will have Topic-A/Partition_0(Leader)
- Broker_102 will have Topic-A/Partition_0(ISR)
- Broker_102 will have Topic-A/Partition_1(Leader)
- Broker_103 will have Topic-A/Partition_1(ISR)


## Leader for a Partition
- At any time only ONE broker can be a leader for a given partition
- Only that leader can receive and serve data for a partition
- The other brokers will sync the data
- Therefore each partition has one leader and multiple ISR (in-sync replica)


## Producer
- Producers write data to topics (which is made of partitions)
- Producers automatically know to which broker and partition to write to
- In case of Broker failures, Producers will automatically recover

<img width="500" alt="Producers" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0002.png">


## Producers and acknowledgement
Producers can choose to receive acknowledgement of data writes:
- acks=0: Producer won't wait for acknowledgement (possible data loss)
- acks=1 (default setting): Producer will wait for the leader to acknowledge (limited data loss)
- acks=all: Leader + replicas acknowledgement (no data loss)


## Producer and msg keys
- Producers can choose to send a ket with the message (string, number, etc)
- If key=null, data is sent round robin (broker 101 then 102 then 103.....)
- If a key is sent, then all messages for that key will always go to the same partition
- A key is basically sent if you need message ordering for a specific field

<img width="500" alt="ProducersAndKeys" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0003.png">


## Consumers
- Consumers read data from a topic (identified by name)
- Consumers know which broker to read from
- In case of broker failures, consumers know how to recover
- Data is read in order within each partitions
- Consumer read in order

<img width="500" alt="Consumers" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0004.png">


- Consumer can read from multiple par partitions (at a high level the consumer is reading them in parallel, in the implementation side of things - the consumer will actually read a little bit of one partition (e.g 1) and then a little bit from another partition (e.g 2) and etc...)  There is no specific order guaranteed..

## Consumer Groups
- Consumers read data in consumer groups
- Each consumer within a group reads from exclusive partitions

<img width="500" alt="ConsumerGroups" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0005.png">

## Consumer Groups (too many consumers)
- If you have more consumers than partitions, some consumers will be inactive
- Sometimes we will want that (e.g if Consumer 3 dies, consumer 4 will takes its place)
- But in general we should have as many partitions as consumers

<img width="500" alt="TooManyConsumers" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0006.png">


## Consumer Offsets
- Kafka stores the offsets at which a consumer group has been reading
- The offsets committed live in a Kafka topic names __consumer__offsets
- When a consumer in a group has processed data received from Kafka, it should be committing the offsets
- If a consumer dies, it will be able to read back from where if left off thanks to the committed consumer offsets!


## Delivery semantics for consumers
Consumer choose when to commit offsets and there are 3 delivery semantics:
> **At most once: (not preferred)** 
> * offsets are committed as soon as the message is received
> * if the processing goes wrong, the message will be list (it won't be read again)

> **At least once: (usually preferred)** 
> * offsets are committed after the message is processed
> * if the processing goes wrong, the message will be read again
> * this can result in duplicate processing of messages. Make sure your processing is **idempotent** (i.e processing again the message won't impact your systems)

> **Exactly once** 
> * can be achieved for Kafka => Kafka workflows using Kafka Stream API
> * For Kafka => External System workflows, use an idempotent consumer


## Kafka Broker Discovery
- Every Kafka broker is called a "bootstrap server"
- That means that you only need to connect to one broker, and you will be connected to the entire cluster
- Each broker knows about all brokers, topics and partitions (metadata)

<img width="500" alt="Broker_Discovery" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0007.png">


## Zookeeper
- Zookeeper manages brokers (keeps a list of them)
- Zookeeper helps in performing leader election for partitions
- Zookeeper sends notifications to Kafka in case of changes (e.g new topic, broker dies, broker comes up, delete topics, etc...)
- **Kafka can't work without Zookeeper**
- Zookeeper by design operates with an **odd** number of servers (3, 5, 7)
- Zookeeper has a leader (handle writes) the rest of the servers are followers (handle reads)
- Consumers and producers read and write to kafka (It's Kafka that uses Zookeeper to write to it's metadata)
- Zookeeper does **NOT** store consumer offsets with Kafka > v0.10


## Kafka Guarantees
- Messages are appended to a topic-partition in the order they are sent
- Consumers read messages in the order stored in a topic-partition
- With a replication factor of N, producers and consumers can tolerate up to N-1 brokers being down
- This is why a replication factor of 3 is a good idea:
  * Allows for one broker to be taken down for maintenance
  * Allows for another broker to be taken down unexpectedly
- As long as the number of partitions remains constant for a topic (no new partitions), the same key will always go to the same partition


## Summary
<img width="700" alt="Broker_Discovery" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0008.png">

___

## Start zookeeper
``` bash
zookeeper-server-start.sh ./config/zookeeper.properties
```

## Start kafka broker
``` bash
kafka-server-start.sh config/server.properties
```

## Create topics
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --create --partitions 6 --replication-factor 1
```

> **--replication-factor cannot be greater than number of brokers**

## List of topics 
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --list
```

## Topic details
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --describe
```

- Leader is "broker 0"
- Replicas is "broker 0"
- Isr is "broker 0"
<img width="500" alt="Broker_Discovery" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0009.png">

``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --describe
```

## Delete topics
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --delete
```
- By default, delete.topic.enable is set to true

## Producer (default props)
``` bash
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic
```

## Producer (custom props)
``` bash
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all
```

## Producer (write to non existing topic -> will be created on the fly)
``` bash
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic new_topic

kafka-topics.sh --zookeeper 127.0.0.1:2181 --list

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic new_topic --describe
```

**Default** - creates topic with Partition qty of 1 and Replication factor qty of 1

**Best practice** - always create topic beforehand

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

## Producer with keys
``` bash
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
```
> key,value
> another key,another value


## Consumer
- Command only intercepts msg as from launch - won't retrieve missed msg
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic 
```

## Consumer (read from beginning)
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning
```
- However does not come in the order sent because of different partitions in the topic

## Consumer Same Group
Console 1
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application
```
Console 2
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application
```
Console 3
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application
```

## Consumer Different Group
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-application --from-beginning
```

- If **--from-beginning** is used for a specific group, only the first consumer will receive all the msg, the second consumer in the same group will start where the offset was last committed by the first consumer


## Consumer Groups
``` bash
kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list
```
- When use consumer and don't specify groups -> it generates a random one

``` bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-second-application

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-first-application
```

## Resetting Offsets
``` bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-second-application --reset-offsets --to-earliest --execute --topic first_topic

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-second-application --reset-offsets --shift-by -2 --execute --topic first_topic
```
> --shift-by -2 (negative means move back by 2)
> --shift-by 2 (positive means move forward by 2)
> **IMPORTANT** - command (--shift-by) will be applied to all partitions in the topic (e.g if there are 3 partitions and we shift by -2, there will be 6 (3 partitions * 2 offsets) messages consumed)


## Consumer with keys
``` bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,
```

___
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-third-application
```