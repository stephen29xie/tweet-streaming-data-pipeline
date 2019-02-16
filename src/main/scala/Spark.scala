package spark

import mongodb.MongoDBConnection

import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.Row
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.ForeachWriter


import com.typesafe.config.ConfigFactory

object SparkStructuredStreamer {

	val mongodbconfig = ConfigFactory.load().getConfig("mongodb")
	val sparkconfig = ConfigFactory.load().getConfig("spark")

	val spark = SparkSession.builder
							.master(sparkconfig.getString("MASTER_URL"))
							.appName("TweetStream")
							.getOrCreate()

	spark.sparkContext.setLogLevel("WARN")

	import spark.implicits._

	val kafkaconfig = ConfigFactory.load().getConfig("kafka")

	// This is the Spark Structured Streaming + Kafka integration
	// Do not have to explicitly use the Consumer API to consume from kafka
	val ds = spark.readStream
				  .format("kafka")
				  .option("kafka.bootstrap.servers", kafkaconfig.getString("BOOTSTRAP_SERVERS"))
				  .option("subscribe", kafkaconfig.getString("TOPIC"))
				  .load()

	/*
	This is the schema of a consuming a ProducerRecord from kafka. Value is the actual payload and
	the rest of the fields are metadata

	root
	 |-- key: binary (nullable = true)
	 |-- value: binary (nullable = true)
	 |-- topic: string (nullable = true)
	 |-- partition: integer (nullable = true)
	 |-- offset: long (nullable = true)
	 |-- timestamp: timestamp (nullable = true)
	 |-- timestampType: integer (nullable = true)

	 At this point, our key and values are in UTF8/binary, which was serialized this way by the
	 KafkaProducer for transmission of data through the kafka brokers.

	 from https://spark.apache.org/docs/2.4.0/structured-streaming-kafka-integration.html
	 "Keys/Values are always deserialized as byte arrays with ByteArrayDeserializer. 
	 Use DataFrame operations to explicitly deserialize the keys/values"
	*/

	// Transforms and preprocessing can be done here
	val selectds = ds.selectExpr("CAST(value AS STRING)") // deserialize binary back to String type

	// // We must create a custom sink for MongoDB
	// // ForeachWriter is the contract for a foreach writer that is a streaming format that controls streaming writes.
	val customwriter = new ForeachWriter[Row] {
		def open(partitionId: Long, version: Long): Boolean = {
	    	true
	    }
	    def process(record: Row): Unit = {
		    // Write string to connection
		    MongoDBConnection.insert(record(0).toString())
	    }
	    def close(errorOrNull: Throwable): Unit = {
	    	Unit
    	}
  	}

	val writedf = selectds.writeStream
						  .foreach(customwriter)
						  .start()
	writedf.awaitTermination()

	// spark.stop()

}