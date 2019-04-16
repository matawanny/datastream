export PATH=$PATH:/opt/cloudera/parcels/KAFKA/lib/kafka/bin

bin/kafka-start-server.sh config/server.properties
bin/kafka-start-server.sh config/server1.properties
bin/kafka-start-server.sh config/server2.properties

#in machine ybgdev96
#Create topic 
kafka-topics.sh --create --topic my-first-topic -zookeeper ybgdev96.ny.ssmb.com:2181 --replication-factor 1 --partitions 1

#Describe topic
kafka-topics.sh --describe -zookeeper ybgdev96.ny.ssmb.com:2181

#instantiate a Console Producer
kafka-console-producer.sh --broker-list ybrdev96:9092 --topic my-first-topic

#instantiate a Console Consumer
kafka-console-consumer.sh --bootstrap-server ybgdev96:2181 --topic my-first-topic --from-beginning

#How to delete a topic?
kafka-topics.sh --delete -zookeeper ybgdev96.ny.ssmb.com:2181 --topic my-first-topic

#Alter a topic
kafka-topics.sh --alter -zookeeper ybgdev96.ny.ssmb.com:2181 --topic my-first-topic --partitions 4

#Kill Kafka Server

ps ax | grep -i 'kafka\.Kafka'

kill -9 <processId>
___________________________________________________________________________________________________
#multiple brokers
#Start 3 kafka brokers
kafka-server-start.sh /opt/cloudera/parcels/KAFKA/etc/kafka/conf.dist/server.properties

cd $KAFKA_HOME/bin
kafka-server-start.sh ../config/server.properties

kafka-server-start.sh ../config/server1.properties

kafka-server-start.sh ../config/server2.properties

# Create a topic with partitions
./kafka-topics.sh --create --topic my-second-topic -zookeeper ybgdev96.ny.ssmb.com:2181 --replication-factor 3 --partitions 3

# Check topics
./kafka-topics.sh --describe --zookeeper ybgdev96.ny.ssmb.com:2181

# Instantiate a Console Producer
./kafka-console-producer.sh --broker-list ybrdev96:9092 --topic my-second-topic

#Instantiate a Console Consumer
./kafka-console-consumer.sh --bootstrap-server ybgdev96:9092 --topic my-second-topic --from-beginning

./kafka-console-consumer.sh --bootstrap-server ybgdev96:9092 --topic my-second-topic --from-beginning
__________________________________________

kafka-topics.sh -zookeeper ybgdev96.ny.ssmb.com:2181 --topic first_topic --create --partitions 3 --replication-factor 2

This command will give you the list of the active brokers between brackets:

./zookeeper-shell.sh localhost:2181 <<< "ls /brokers/ids"

kafka-tpoics --zookeeper  ybgdev96.ny.ssmb.com:2181 --list
kafka-console-producer --broker-list ybgdev96.ny.ssmb.com:9092 --topic first_topic
kafka-console-producer --broker-list ybgdev96.ny.ssmb.com:9092 --topic first_topic --producer-property acks=all
kafka-console-producer --broker-list ybgdev96.ny.ssmb.com:9092 --topic new_topic
kafka-console-consumer --bootstrap-server ybgdev96.ny.ssmb.com:9092 --topic first_topic --from-beginning
kafka-console-consumer --bootstrap-server ybgdev96.ny.ssmb.com:9092 --topic first_topic --group my-first-application
kafka-console-consumer --bootstrap-server ybgdev96.ny.ssmb.com:9092 --topic first_topic --group my-second-application
kafka-consumer-groups --bootstrap-server ybgdev96.ny.ssmb.com:9092 --list
kafka-consumer-groups --bootstrap-server ybgdev96.ny.ssmb.com:9092 --describe --group y-first-application
kafka-consumer-groups --bootstrap-server ybgdev96.ny.ssmb.com:9092 --group my-first-application --reset-offsets --to-earliest --execute --topic first topic

cd $KAFKA_HOME
./kafka-topics.sh --create --topic embs-topic -zookeeper ybgdev96.ny.ssmb.com:2181 --replication-factor 3 --partitions 3
./kafka-console-producer.sh --broker-list ybgdev96:9092 --topic embs-topic

./kafka-topics.sh --create --topic embs-topic -zookeeper  ybgdlnn2b:2181 --replication-factor 3 --partitions 3
./kafka-topics.sh --create --topic first-topic -zookeeper ybgdldn2c:2181 --replication-factor 3 --partitions 3

./kafka-topics.sh --describe --topic first-topic -zookeeper ybgdldn2c:2181



./kafka-console-producer.sh --broker-list ybgdlnn2c:9092 --topic embs-topic
./kafka-topics.sh --describe --topic embs-topic -zookeeper ybgdlnn2b.ny.ssmb.com:2181

./kafka-console-consumer.sh --bootstrap-server ybgdev96:9092 --topic embs-topic --from-beginning

./kafka-topics.sh --create --topic twitter_tweets -zookeeper ybgdlnn2b.ny.ssmb.com:2181 --replication-factor 3 --partitions 6
kafka-console-consumer --bootstrap-server ybgdev96.ny.ssmb.com:9092 --topic twitter_tweets


./kafka-topics.sh --create --topic access-log -zookeeper ybgdev96:2181 --replication-factor 3 --partitions 3
kafka-console-consumer --bootstrap-server ybgdev96.ny.ssmb.com:9092 --topic access-log --group ddos-application