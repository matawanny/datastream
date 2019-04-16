package com.phdata.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerFromLog {

	static private String fileName = "apache-access-log.txt";
	private KafkaProducer<String, String> producer;
	private String bootstrapServers = "";
	private String topic = "";
	private String directory = "";

	Logger logger = LoggerFactory.getLogger(KafkaProducerFromLog.class);

	KafkaProducerFromLog() {

		String propFileName = "config.properties";
		Properties prop = new Properties();

		try (InputStream inputStream = KafkaProducerFromLog.class
				.getClassLoader().getResourceAsStream(propFileName)) {
			// Loading the properties.
			prop.load(inputStream);
			// Getting properties
			bootstrapServers = prop.getProperty("bootstrap.servers");
			topic = prop.getProperty("topic");
			directory = prop.getProperty("directory");
			logger.info("bootstrapServer="+bootstrapServers);
			logger.info("topic="+topic);
		} catch (IOException ex) {
			logger.error("Problem occurs when reading file !", ex);
		}

		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				bootstrapServers);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class.getName());
		
		// create Producer
		producer = new KafkaProducer<String, String>(
				properties);		
		
		//add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("stoppinp application...");
			logger.info("shutting down directory watcher...");

			logger.info("closing producer...");
			producer.close();
			logger.info("done!");
		}));

	}

	private void sendMessage(String topic, String msg) {
		producer.send(new ProducerRecord<String, String>(topic,null, msg), new Callback() {
			@Override
			public void onCompletion(RecordMetadata recordMetadata, Exception e) {
				if(e!=null){
					logger.error("Something bad happend when sending message", e);
				}
			}
		});
		// flush data
		producer.flush();
	}
	

	private void processEvents() throws IOException {
		String inputFile = directory + "/" + fileName;
		FileReader reader = new FileReader(new File(inputFile));
    	BufferedReader bufferedReader = new BufferedReader(reader);
    	String line;
    	while((line = bufferedReader.readLine())!=null )
    	{
    		sendMessage(topic, line);
    	}
    	sendMessage(topic, "FINISH");
    	bufferedReader.close();
	}
	
	
	public static void main(String[] args) throws IOException {
		new KafkaProducerFromLog().processEvents();
	}
}