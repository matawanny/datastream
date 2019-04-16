package com.phdata.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.phdata.models.HitStructure;
import com.phdata.models.Record;



public class DdosDetectKafkaConsumer {

	private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss",Locale.US);
	private static String outputFileName = "suspicious-ipaddress.txt";
	public static void main(String[] args) throws Exception {
		Calendar cal = Calendar.getInstance();
    	System.out.println("[" + sdf.format(cal.getTime())
				+ "] The File Process server is running." );
		new DdosDetectKafkaConsumer().run();
	}
	
	private DdosDetectKafkaConsumer(){}
	
	private void run() {
		Logger logger=LoggerFactory.getLogger(DdosDetectKafkaConsumer.class);

		String propFileName = "config.properties";
		Properties prop = new Properties();
		String bootstrapServers = "";
		String topic = "";
		String groupId = "";
		String directory= "";
		PrintWriter out = null;

		try (InputStream inputStream = DdosDetectKafkaConsumer.class
				.getClassLoader().getResourceAsStream(propFileName)) {
			// Loading the properties.
			prop.load(inputStream);
			// Getting properties
			bootstrapServers = prop.getProperty("bootstrap.servers");
			logger.info("bootstrapServers="+bootstrapServers);
			topic = prop.getProperty("topic");
			logger.info("topic="+topic);
			groupId = prop.getProperty("group.id");
			logger.info("groupId="+groupId);
			directory = prop.getProperty("directory");
			logger.info("directory="+directory);
			
			File f= new File (directory+"//"+outputFileName);
/*			if(f.exists() && !f.isDirectory()) { 
			    f.delete();
			}*/
			FileWriter fw = new FileWriter(directory+"//"+outputFileName, true);
		    out = new PrintWriter(new BufferedWriter(fw));
			
		} catch (IOException ex) {
			logger.error("Problem occurs when reading file !", ex);
		}
		
		
		// latch for dealing with multiple threads
		CountDownLatch latch = new CountDownLatch(1);
		
		// create the consumer runnable
		logger.info("Creating the consumer thread");
		Runnable FileProcessRunnable = new FileProcessConsumberRunnable(bootstrapServers,groupId,topic,latch,out);
		
		// start the thread
		Thread myThread = new Thread(FileProcessRunnable);
		myThread.start();
		
		// add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread ( () -> {
			logger.info("Caught shutdown hook");
			((FileProcessConsumberRunnable) FileProcessRunnable).shutdown();
			try {
				latch.await();
			} catch (Exception e) {
				logger.error("Error in latch await. ",e);
			}
			logger.info("Application has existed.");
		}
		));
		
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Application got interupted", e);
		} finally {
			logger.info("Application is closing.");
		}	
		
	}

    private static class FileProcessConsumberRunnable implements Runnable {

		private CountDownLatch latch;
		private KafkaConsumer<String, String> consumer;
		private Logger logger=LoggerFactory.getLogger(FileProcessConsumberRunnable.class); 
        private PrintWriter outputfile;
        private static Integer LIMIT=88;

    	private static final Map<String,HitStructure> mapOfRecords = new HashMap<>();
    	private static final Set<String> suspiciousIPs = new HashSet<>();
        //private final HashMap<String, Date> messages;

        public FileProcessConsumberRunnable(String bootstrapServers, 
				  String groupId,
				  String topic,
				  CountDownLatch latch, 
				  PrintWriter outputfile) {
        	this.latch = latch;
            this.outputfile = outputfile;
         
			// create consumer properties
			Properties properties=new Properties();
			properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			this.consumer = new KafkaConsumer<String,String>(properties);
			// subscribe consumer to our topic(s)
			this.consumer.subscribe(Arrays.asList(topic));
        }

		public void run() {
			Calendar cal=null;
			try {
				while (true) {
					//String message = in.readLine();
					ConsumerRecords<String, String> records = consumer
							.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0					
					cal = Calendar.getInstance();
					for (ConsumerRecord<String, String> record : records) {
/*						logger.info("Key: " + record.key() + ", Value: "
								+ record.value());
						logger.info("Partition " + record.partition()
								+ ", Offset: " + record.offset());*/
						String message=record.value();
/*						logger.info("[" + sdf.format(cal.getTime())
								+ "] Incoming message=" + message);*/
						
						if(message.equalsIgnoreCase("FINISH"))
							this.outputfile.close();
						else{
							processMessage(message);
						}
				
					}					
				}
			} catch (WakeupException e) {
				logger.info("Received shutdown signal!");
			} finally {
				consumer.close();
				// tell our main code we're done with the consumer
				latch.countDown();
			}				
		}
        
		
		public void processMessage(String message){
	   		//parses desired data from records
    		Record r = splitFields(message);
    		String ipAddress = r.getIpAddress();
    		System.out.println("Currently Processsing IP :" + ipAddress);
    		Date currentTime = r.getTimestamp();
    		//checks for occurence of IP in map
			if(mapOfRecords.containsKey(ipAddress))
    		{
    			Date lastHitTime = mapOfRecords.get(ipAddress).getTimestamp();
    			//Calculates time difference
				long diff = currentTime.getTime()-lastHitTime.getTime();
    			long diffSeconds = diff /1000%60;
    			long diffMinutes = diff /(60*1000)%60;
    			long diffHours = diff /(60*60*1000);
    			if(diffHours==0 && diffMinutes<1)
    			{
    				int updateCount = mapOfRecords.get(ipAddress).getCount();
    				HitStructure h = new HitStructure(lastHitTime,updateCount+1);
    				mapOfRecords.put(ipAddress, h);
    				//Checks whether the current count exceeds the threshold or not
					if(updateCount+1 >=LIMIT)
    				{
    					// Adds the suspicious IP to the final result set
						if(!suspiciousIPs.contains(ipAddress))
    					{
	    					suspiciousIPs.add(ipAddress);
	    					outputfile.println("IP Address : " + ipAddress);
	    				}
    				}
    			}
    		}
    		else
    		{
    			//Resets the entry count to 1
				HitStructure h = new HitStructure(currentTime,1);
    			mapOfRecords.put(ipAddress, h);
    		}
		}
		


		public void shutdown() {
			// the wakeup() method is a special method to interrupt consumer.poll()
			// it will thrown the exception WakeUpException
			consumer.wakeup();
			
		}
		
		// Core function for parsing desired data from records
		public static Record  splitFields(String line)
		{
			String[] fields = line.split(" ");
			String timeStamp = fields[3].substring(1,fields[3].length()).split(" ")[0];
			Date d = null;
			try {
				d = df.parse(timeStamp);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Record r= new Record(fields[0],d);
			System.out.println(r.getIpAddress()+" "+r.getTimestamp());
			return r;
		}
		
    }
}
