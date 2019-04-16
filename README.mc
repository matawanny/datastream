This is a project for detection of fraud IP Addresses and the number of times they have tried to hit the server, This can be used to avoid DDoS (Distributed Denial of Service) Attack.



1. copy apache_access_log into /usr/book/output

2. deploy the code into /usr/book/repository/com/phdata/datastream/0.0.1/datastream-0.0.1-shaded.jar

3. create a topic: 
	./kafka-topics.sh --create --topic access-log -zookeeper ybgdev96:2181 --replication-factor 3 --partitions 3

4. start consumer
   java -cp datastream-0.0.1-shaded.jar  com.phdata.engine.KafkaProducerFromLog

5. start producer
 java -cp datastream-0.0.1-shaded.jar  com.phdata.engine.DdosDetectKafkaConsumer
 
6. the out put file is in 
   /usr/book/output/suspicious-ipaddress.txt