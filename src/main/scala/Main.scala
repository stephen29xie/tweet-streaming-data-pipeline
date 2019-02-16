import twitter.TwitterStreamer
import kafka.MessagePublisher
import spark.SparkStructuredStreamer

object Main {
	def main(args: Array[String]) {

  		// // Set up Kafka brokers and KafkaProducer to send messages to brokers
  		MessagePublisher
  		
  		// // Start streaming tweets, which calls the KafkaProducer to send itself to the brokers
  		TwitterStreamer.stream()

  		// // Set up Spark session to read stream (consume) from kafka brokers,
  		// // do any transformations/processing
  		// // and then write stream out to sink (MongoDB)
  		SparkStructuredStreamer
	}
}