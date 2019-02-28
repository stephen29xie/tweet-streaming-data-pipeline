package kafka

import java.util.Properties

// Kafka Producer API
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import com.typesafe.config.ConfigFactory

object MessagePublisher {

	val kafkaconfig = ConfigFactory.load().getConfig("kafka")
	val bootstrapservers:String = kafkaconfig.getString("BOOTSTRAP_SERVERS")

	val props = new Properties()
	props.put("bootstrap.servers", bootstrapservers);
	props.put("acks", "all");
	props.put("delivery.timeout.ms", "30000");
	props.put("batch.size", "16384");
	props.put("linger.ms", "1");
	props.put("buffer.memory", "33554432");
	props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // StringSerializer encoding defaults to UTF8
	props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // StringSerializer encoding defaults to UTF8
	
	
	val producer = new KafkaProducer[String, String](props)

	def sendMessage(topic:String,message:String) {
		producer.send(new ProducerRecord[String,String](topic,message)) // (topic,key,value)
	}

}