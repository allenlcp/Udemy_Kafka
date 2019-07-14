# 1.0 Kafka Theory

<img width="500" alt="Topics, partitions and offsets" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0001.png">

### Topics
Is a particular stream of data
- Similar to a table in a database (without all the constraints)
- You can have as many topics as you want
- A topic is identified by its name


### Topics are split by Partitions
- Each partition is ordered
- Each message within a partition gets an incremental id, called offset


### Offsets
- Offset only have a meaning for a specific partition (e.g offset 3 in partition 0 doesn't represent the same data as offset 3 in partition 1)
- Order only guaranteed only within a partition (not across partitions)
- Data is kept only for a limited time (default in one week)
- Once the data is written to a partition, it can't be changed (immutability)
- Data is assigned randomly to a partition unless a key is provided 


### Brokers
- A Kafka clusters is composed of multiple brokers (servers)
- Each broker is identified with its ID (integer)
- Each broker contains certain topic partitions
- After connecting to any broker (Called a bootstrap broker), you will be connected to the entire cluster
- A good number to get started is 3 brokers, but some big clusters have over 100 brokers


### Brokers and topics
Example of Topic-A with 3 partitions and 100 brokers 
- Broker_101 will have Topic-A/Partition_0
- Broker_102 will have Topic-A/Partition_1
- Broker_103 will have Topic-A/Partition_3

Example of Topic-A with 2 partitions and 100 brokers 
- Broker_101 will have Topic-A/Partition_0
- Broker_102 will have Topic-A/Partition_1


### Replication
- Topic replication factor > 1 (usually between 2 and 3)
- This way if a broker is down, another broker can serve the data

Example: Topic-A with 2 partitions and replication factor of 2 (2 copies)
- Broker_101 will have Topic-A/Partition_0(Leader)
- Broker_102 will have Topic-A/Partition_0(ISR)
- Broker_102 will have Topic-A/Partition_1(Leader)
- Broker_103 will have Topic-A/Partition_1(ISR)


### Leader for a Partition
- At any time only ONE broker can be a leader for a given partition
- Only that leader can receive and serve data for a partition
- The other brokers will sync the data
- Therefore each partition has one leader and multiple ISR (in-sync replica)


### Producer
- Producers write data to topics (which is made of partitions)
- Producers automatically know to which broker and partition to write to
- In case of Broker failures, Producers will automatically recover

<img width="500" alt="Producers" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0002.png">


### Producers and acknowledgement
Producers can choose to receive acknowledgement of data writes:
- acks=0: Producer won't wait for acknowledgement (possible data loss)
- acks=1 (default setting): Producer will wait for the leader to acknowledge (limited data loss)
- acks=all: Leader + replicas acknowledgement (no data loss)


### Producer and msg keys
- Producers can choose to send a ket with the message (string, number, etc)
- If key=null, data is sent round robin (broker 101 then 102 then 103.....)
- If a key is sent, then all messages for that key will always go to the same partition
- A key is basically sent if you need message ordering for a specific field

<img width="500" alt="ProducersAndKeys" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0003.png">


### Consumers
- Consumers read data from a topic (identified by name)
- Consumers know which broker to read from
- In case of broker failures, consumers know how to recover
- Data is read in order within each partitions
- Consumer read in order

<img width="500" alt="Consumers" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0004.png">


- Consumer can read from multiple par partitions (at a high level the consumer is reading them in parallel, in the implementation side of things - the consumer will actually read a little bit of one partition (e.g 1) and then a little bit from another partition (e.g 2) and etc...)  There is no specific order guaranteed..

### Consumer Groups
- Consumers read data in consumer groups
- Each consumer within a group reads from exclusive partitions

<img width="500" alt="ConsumerGroups" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0005.png">

### Consumer Groups (too many consumers)
- If you have more consumers than partitions, some consumers will be inactive
- Sometimes we will want that (e.g if Consumer 3 dies, consumer 4 will takes its place)
- But in general we should have as many partitions as consumers

<img width="500" alt="TooManyConsumers" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0006.png">


### Consumer Offsets
- Kafka stores the offsets at which a consumer group has been reading
- The offsets committed live in a Kafka topic names __consumer__offsets
- When a consumer in a group has processed data received from Kafka, it should be committing the offsets
- If a consumer dies, it will be able to read back from where if left off thanks to the committed consumer offsets!


### Delivery semantics for consumers
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


### Kafka Broker Discovery
- Every Kafka broker is called a "bootstrap server"
- That means that you only need to connect to one broker, and you will be connected to the entire cluster
- Each broker knows about all brokers, topics and partitions (metadata)

<img width="500" alt="Broker_Discovery" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0007.png">


### Zookeeper
- Zookeeper manages brokers (keeps a list of them)
- Zookeeper helps in performing leader election for partitions
- Zookeeper sends notifications to Kafka in case of changes (e.g new topic, broker dies, broker comes up, delete topics, etc...)
- **Kafka can't work without Zookeeper**
- Zookeeper by design operates with an **odd** number of servers (3, 5, 7)
- Zookeeper has a leader (handle writes) the rest of the servers are followers (handle reads)
- Consumers and producers read and write to kafka (It's Kafka that uses Zookeeper to write to it's metadata)
- Zookeeper does **NOT** store consumer offsets with Kafka > v0.10


### Kafka Guarantees
- Messages are appended to a topic-partition in the order they are sent
- Consumers read messages in the order stored in a topic-partition
- With a replication factor of N, producers and consumers can tolerate up to N-1 brokers being down
- This is why a replication factor of 3 is a good idea:
  * Allows for one broker to be taken down for maintenance
  * Allows for another broker to be taken down unexpectedly
- As long as the number of partitions remains constant for a topic (no new partitions), the same key will always go to the same partition


### Summary
<img width="700" alt="Broker_Discovery" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0008.png">

___

# 2.0 Kafka CLI

## 2.1 Kafka / Zookeeper admin command

### Start zookeeper
``` bash
zookeeper-server-start.sh ./config/zookeeper.properties
```

### Start kafka broker
``` bash
kafka-server-start.sh config/server.properties
```

### Create topics
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --create --partitions 6 --replication-factor 1
```

> **--replication-factor cannot be greater than number of brokers**

### List of topics 
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --list
```

### Topic details
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

### Delete topics
``` bash
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --delete
```
- By default, delete.topic.enable is set to true

___ 

## 2.2 Producer/ Consumer - Message __without__ keys

### Producer (default props)
``` bash
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic
```

### Producer (custom props)
``` bash
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all
```

### Producer (write to non existing topic -> will be created on the fly)
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
___

### Consumer
- Command only intercepts msg as from launch - won't retrieve missed msg
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic 
```

### Consumer (read from beginning)
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning
```
- However does not come in the order sent because of different partitions in the topic

### Consumer Same Group
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

### Consumer Different Group
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-application --from-beginning
```

- If **--from-beginning** is used for a specific group, only the first consumer will receive all the msg, the second consumer in the same group will start where the offset was last committed by the first consumer


### Consumer Groups
``` bash
kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list
```
- When use consumer and don't specify groups -> it generates a random one

``` bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-second-application

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-first-application
```

### Resetting Offsets
``` bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-second-application --reset-offsets --to-earliest --execute --topic first_topic

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-second-application --reset-offsets --shift-by -2 --execute --topic first_topic
```
> --shift-by -2 (negative means move back by 2)

> --shift-by 2 (positive means move forward by 2)

> **IMPORTANT** - command (--shift-by) will be applied to all partitions in the topic (e.g if there are 3 partitions and we shift by -2, there will be 6 (3 partitions * 2 offsets) messages consumed)

___


## 2.3 Producer/ Consumer - Message __with__ keys
### Producer with keys
``` bash
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
```
> key,value
> another key,another value

### Consumer with keys
``` bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-application --from-beginning --property print.key=true --property key.separator=,
```

___

https://github.com/edenhill/kafkacat
https://medium.com/@coderunner/debugging-with-kafkacat-df7851d21968

___

# 3.0 Kafka Java Programming

``` bash
## Starting zookeeper
zookeeper-server-start.sh ./config/zookeeper.properties

## Starting broker
kafka-server-start.sh config/server.properties

## Creating topic
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --create --partitions 3 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic twitter_tweets --create --partitions 6 --replication-factor 1

## Topic details
kafka-topics.sh --zookeeper 127.0.0.1:2181 --list

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --describe

kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic twitter_tweets --describe

## Run consumer
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic twitter_tweets --group my-third-application --from-beginning

## Describe my-third-application group
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-third-application
```


___

# 4.0 Kafka Producer Configuration
https://kafka.apache.org/documentation/#producerconfigs

## Producer config - acknowledgement deep dive
acks=all: Leader + replicas acknowledgement (no data loss)
- must be used in conjunction with min.insync.replicas
``` bash
min.insync.replicas can be set at the broker or topic level (override)

min.insync.replicas=2 implies that at least 2 brokers that are ISR (including leader) must respond that they have the data
```
- than means that if you use **replication.factor=3,mininsync=2,acks=all**, you can only tolerate 1 broker going down, otherwise the producer will receive an exception on send

## Producer config - retries
**"retries"** setting 
- defaults to 0
- you can increase to a high number, e.g Integer.MAX_VALUE
- in case of retries, by default there is a chance that message will be sent out of order (if a batch has failed to be sent)
- for this, you can set the setting while controls how many produce requests can be made in parallel: **max.inflight.requests.per.connection**
> Default:5
> See it to 1 if you need to ensure ordering (may impact throughput)

## Producer config - idempotent
Idempotent producers are great to guarantee a stable and safe pipeline

They come with:
- retries: Integer.MAX_VALUE (2^31-1 = 2147483647)
- max.in.flight.requests=1 (Kafka >= -.11 &< 1.1) or
- max.in.flight.requests=1 (Kafka >= 1.1 - higher performance) 
- acks=all

``` bash
producerProps.put("enable.idempotence", true);
```

## Summary producer config - safe producer
Kafka < 0.11
- acks=all (producer level) 
> Ensures data is properly replicated before an ack is received

- min.insync.replicas=2 (broker/topic level)
> Ensures two brokers in ISR at least have the data after an ack

- retries=MAX_INT (producer level)
> Ensures transient errors a retrieved indefinitely

- max.in.flight.requests=1 (producer level)
> Ensures only one request is tried at any time, preventing message re-ordering in case of retries


Kafka >= 0.11
- enable.idempotence=true (producer level) + min.insync.replicas=2 (broker/topic level)
> Implies acks=all,retries=MAX_INT,max.in.flight.requests=5 (default)
> while keeping ordering guarantees and improving performance

``` bash
## create safe producer
properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // kafka > >= 1.1
```

**Note:** running a "safe producer" might impact throughput and latency, always test for you use case 


## Producer config - message compression
- Producer usually send data that is text-based, for example with JSON data
- In this case, it is important to apply compression to the producer
- Compression is enabled at the Producer level and doesn't require any configuration change in the Brokers or in the Consumers
- **"compression.type"** can be **'none'** (default), **'gzip'**,**'lz4'**,**'snappy'**
- Compression is more effective the bigger the batch of message being sent to Kafka!
- The compressed batch has the following advantage:
> - Much smaller producer request size (compression ration up to 4x!)
> - Faster to transfer data over the network => less latency
> - Better throughput
> - Better disk utilisation in Kafka (stored messages on disk are smaller)

- Disadvantage (very minor):
> - Producers must commit some CPU cycles to compression
> - Consumers must commit some CPU cycles to decompression

Overall 
> - Consider testing snappy or lz4 for optimal speed / compression ratio


## Producer config - linger.ms & batch.size
By default Kafka tries to send records as soon as possible
- It will have up to 5 requests in flight, meaning up to 5 messages individually sent at the same time
- After this, if more messages have to be sent while others are in flight, Kafka is smart and will start batching them while they wait to send them all at once

This is smart batching allows Kafka to increase throughput while maintaining very low latency

Batches have higher compression ratio so better efficiency

- **Linger.ms** - Number of milliseconds a producer is willing to wait before sending a batch out. (default 0)
> - By introducing some lag (for example linger.ms=5), we increase the chances of messages being sent together in a batch
> - So at the expense of introducing a small delay, we can increase throughput, compression and efficiency of our producer
> - If a batch is full (see batch.size) before the end of the linger.ms period, it will be sent to Kafka right away!

- **batch.size** - Maximum number of bytes that will be included in a batch. The default is 16KB
> - Increasing the batch size to something like 32KB or 64KB can help increasing the compression, throughput, and efficiency of requests
> - Any message that is bigger than the batch size will not be batched
> - A batch is allocated per partition, so make sure that you don't see it to a number that's too high, otherwise you'll run waste memory!
> (Note: You can monitor the average batch size metric using Kafka Producer Metrics)

``` bash
## high throughput producer (at expense of latency and cpu usage)
properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));
```

## Producer config - default partitioner and how keys are hashed
- By default, your keys are hashed using the "murmur2" algorithm
- It is most likely preferred to not override the behavior of the partitioner, but it is possible to do so (partitioner.class)
- The formula is:
``` bash
targetPartition = Utils.abs(Utils.murmur2(recod.key())) % numPartitions;
```
- This means that the same key will go to the same partition (we already know this), and adding partitions to a topic will completely alter the formula


## Producer config - max.block.ms & buffer.memory
- If the producer produces faster than the broker can take, the records will be buffered in memory
- buffer.memory=33554432 (32MB): the size of the send buffer
- That buffer will fill up over time and will back down when the throughput to the broker increases
- If that buffer is full (all 32 MB), then the.send() method will start to block (won't return right away)
- **max.block.ms=60000** -> the time the.send() will block until throwing an exception. Exception are basically thrown when
> - The producer has filled up its buffer
> - The broker is not accepting any new data
> - 60 seconds has elapsed
- If you hit an exception hit that usually means your brokers are down or overloaded as they can't respond to requests

___

# 5.0 Kafka Consumer Configuration

## Consumer config - delivery semantics 

**At most once:** -> offsets are committed as soon as the message batch is received.  If the processing goes wrong, the message will be lost (it won't be read again).

<img width="500" alt="at_most_once" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0010.png">


**At least once:** -> offsets are committed after the message is processed.  If the processing goes wrong, the message will be read again.  This can result in duplicate processing of messages.  Make sure your processing is idempotent (i.e processing again the message won't impact your systems)

<img width="500" alt="at_most_once" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0011.png">


**Exactly once:** -> Can be achieved for Kafka => Kafka workflows using Kafka Streams API.  For Kafka => Sink workflows, use an idempotent consumer.

**Bottom Line:** for most applications you should use **at least once processing** and ensure your transformations / processing are idempotent

## Consumer config - consumer offset commits strategies
There are two most common patterns for committing offsets in a consumer application

1. (easy) enable.auto.commit = true & synchronous processing of batches
``` java
while(true){
    List<Records> batch = consumer.poll(Duration.ofMillis(100)); 
    doSomethingSynchronous(batch)
}
```
- With auto-commit, offsets will be committed automatically for you at regular interval **(auto.commit.interval.ms=5000 by default)** every-time you call .poll()
- If you don't use synchronous processing, you will be in "at-most-once" behavior because offsets will be committed before your data is processed


2. (medium) enable.auto.commit = false & manual commit of offsets
``` java
...
properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // disable auto commit of offsets
properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"); // disable auto commit of offsets
...

while(true){
    batch += consumer.poll(Duration.ofMillis(100));
    if isReady(batch){
        doSomethingSynchronous(batch)
        consumer.commitSync();
    }
}
```
- You control when you commit offsets and what's the condition for the committing them.
- Example, accumulating records into a buffer and then flushing the buffer to a database + committing offsets then


## Consumer config - consumer offset reset behavior

The behavior for the consumer is to then use:
- **auto.offset.reset=latest** will read from the end of the log
- **auto.offset.reset=earliest** will read from the start of the log
- **auto.offset.reset=none** will throw an exception if no offset is found

Additionally, consumer offsets can be lost:
- if a consumer hasn't read new data in 1 day (Kafka < 2.0)
- if a consumer hasn't read new data in 7 day (Kafka >= 2.0)

This can be controlled by the broker setting **offset.retention.minutes**

To replay data for a consumer group:
- Take all the consumers from a specific group down
- Use 'kafka-consumer-groups' command to set offset to what you want
- Restart consumers

**Bottom line**
- Set proper data retention period & offset retention period
- Ensure the auto offset reset behavior is the one you expect / want
- Use replay capability in case of unexpected behavior


## Consumer config - controlling consumer liveliness

<img width="700" alt="controlling consumer liveliness" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0012.png">

## Consumer config - heartbeat thread

**session.timeout.ms** (default 10 seconds):
- Heartbeats are sent periodically to the broker
- If no heartbeats is sent during that period, the consumer is considered dead
- Set even lower to faster consumer re-balances

**heartbeat.interval.ms** (default 3 seconds):
- How often to sent heartbeats
- Usually set to 1/3rd of sessions.timeout.ms

**Take-away** -> This mechanism is used to detect a consumer application being down

## Consumer config - poll thread

**max.poll.interval.ms** (default 5 minutes):
- Maximum amount of time between two.poll() calls before declaring the consumer dead
- This is particularly relevant for Big Data frameworks like Spark in case the processing takes time

**Take-away** -> This mechanism is used to detect a data processing issue with the consumer


___

# 6.0 Kafka Extended API for developers

## Why Kafka Connect and Streams

Four common kafka use cases:

| Use case  | API  | Extended API  |
|---|---|--:|
| Source => Kafka  | Producer API  | Kafka Connect Source  |
| Kafka => Kafka  | Consumer, Producer API  | Kafka Streams  |
| Kafka => Sink  | Consumer API  | Kafka Connect Sink  |
| Kafka => App  | Consumer API  |   |

**Kafka extended API** 
- simplify and improve getting data in and out of Kafka
- simplify transforming data within Kafka without relying on external libs


## Why Kafka Connect
- Programmers always want to import data from the same sources:
<img width="500" alt="controlling consumer liveliness" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0013.png">
- Programmers always want to store data from the same sinks:
<img width="500" alt="controlling consumer liveliness" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0014.png">
- It is tough to achieve Fault Tolerance, Idempotence, Distribution, Ordering
- Other programmers may already have done a very good job!


## Kafka Connect and Streams - architecture design

<img width="500" alt="controlling consumer liveliness" src="https://github.com/allenlcp/Udemy_Kafka/blob/master/resources/images/img_0015.png">

- Kafka connect is on the left hand side


## Kafka Connect - High level
- Source connectors to get data from Common Data Sources
- Sink connectors to publish that data in Common Data Stores
- Make it easy for non-experience dev to quickly get their data reliably into Kafka
- Part of your ETL pipeline
- Scaling made easy from small pipelines to company-wide pipelines
- Re-usable code!

https://www.udemy.com/apache-kafka/learn/lecture/11567132#overview


## Kafka Streams - introduction

https://www.udemy.com/apache-kafka/learn/lecture/11567142#overview


