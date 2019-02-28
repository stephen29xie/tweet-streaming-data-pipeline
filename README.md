# Real-time streaming data pipeline for Twitter Tweets

This is a data pipeline to stream live Tweets through a message broker (Kafka), a processing engine (Spark), and finally storing in a database (MongoDB).

This application uses Docker for containerization.

https://www.docker.com/
> Docker is a computer program that performs operating-system-level virtualization, also known as "containerization".

https://docs.docker.com/compose/
> Compose is a tool for defining and running multi-container Docker applications.


## Architecture Overview
![alt text](images/pipeline_architecture.png)

**Twitter Streaming API**
http://twitter4j.org/en/

Twitter4J is an unofficial Java library for the Twitter API. Using this library, we can stream real-time tweets, and filter to only return Tweets based on keywords, language, location, etc.Individual Tweets objects streamed from this API are in JSON format

**Apache Kafka and Apache Zookeeper**
https://kafka.apache.org/ &
https://zookeeper.apache.org/

Apache Kafka is a distributed streaming platform with multiple capabilities. I am using Kafka in this application as a publish-subscribe messaging system to reliably get data between pipeline components. Tweets are published to a *topic* in the Kafka brokers, where Kafka manages their partitions and order, and eventually consumed by a subscriber.

Kafka uses Apache Zookeeper.

>ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services. All of these kinds of services are used in some form or another by distributed applications.

**Apache Spark Structured Streaming**
https://spark.apache.org/

> Apache Spark is a fast and general-purpose cluster computing system. It provides high-level APIs in Java, Scala, Python and R, and an optimized engine that supports general execution graphs. It also supports a rich set of higher-level tools including Spark SQL for SQL and structured data processing, MLlib for machine learning, GraphX for graph processing, and Spark Streaming.

I use the Structured Streaming API to consume streams of data from Kafka and then write to MongoDB.

> Structured Streaming is a scalable and fault-tolerant stream processing engine built on the Spark SQL engine. You can express your streaming computation the same way you would express a batch computation on static data. The Spark SQL engine will take care of running it incrementally and continuously and updating the final result as streaming data continues to arrive.
> Structured Streaming queries are processed using a micro-batch processing engine, which processes data streams as a series of small batch jobs thereby achieving end-to-end latencies as low as 100 milliseconds and exactly-once fault-tolerance guarantees. 

As the data passes through Spark, I simply filtered each record to only keep the payload (Tweet data), and discarded any Kafka metadata. My focus when doing this project was on the pipeline, but any other preprocessing/transformations/analytics of the streaming data can be defined in this component.

**MongoDB**
https://www.mongodb.com/

MongoDB is an open source, document-oriented, NoSQL database.

> MongoDB stores data in flexible, JSON-like documents, meaning fields can vary from document to document and data structure can be changed over time

MongoDB was my choice of storage because of the JSON format of Tweets. Not all tweets have the same fields.

Sample document in MongoDB:

![alt text](images/sample_tweet.png)

# Usage:
All configuration values are in **application/src/main/resources/application.conf**

1. 
Set the TOPIC to whatever you want to call it.
Do not change BOOTSTRAP_SERVERS as it is already configured to connect to the Docker container that runs Kafka.
```
kafka {
	BOOTSTRAP_SERVERS = "kafka:9092"
	TOPIC = "topicname"
}
```

2. 
Set the Spark MASTER_URL
> ...specifies the master URL for a distributed cluster, or local to run locally with one thread, local[N] to run locally with N threads, or local[*] to run locally with as many worker threads as logical cores on your machine.

```
spark {
	MASTER_URL = "local[*]"
}
```

3. 
MongoDB

Set the DATABASE name and COLLECTION name.
Do not change CONNECTION_STRING as it is already configured to connect to the Docker container that runs MongoDB.
```
mongodb {
	CONNECTION_STRING = "mongodb://mongo:27017"
	DATABASE = "dbname"
	COLLECTION = "collectionname"
}
```

4. 
Twitter API configuration
```
Create a twitter account if you do not already have one.
Go to https://apps.twitter.com/ and log in with your twitter credentials.
Click "Create New App"
Fill out the form, agree to the terms, and click "Create your Twitter application"
In the next page, click on "API keys" tab, and copy your "API key" and "API secret".
Scroll down and click "Create my access token", and copy your "Access token" and "Access token secret".
```
Fill in the keys and tokens
```
twittercredentials {
	CONSUMER_KEY = "..."
	CONSUMER_KEY_SECRET = "..."
	ACCESS_TOKEN = "..."
	ACCESS_TOKEN_SECRET = "..."
}
```

5. 
Change KEYWORDS to be a list of words to filter tweets on. Any tweet that contains these words will be returned in the streaming query. Change LANGUAGES to the languages of tweets you want to return. Currently set to english.
```
tweetfilters {
	KEYWORDS = "Scala,Apache,Spark,Kafka,Data"
	LANGUAGES = "en"
}
```

6.
In the top level directory containing docker-compose.yml,

Run ```docker-compose up``` to create and start the containers. This may take several minutes, and more to download/build the images first.

Once running, you can visit http://localhost:8081 to browse the database on mongo-express, a web-based MongoDB admin interface.

7.
Hit Control-C to gracefully stop the containers.

Run ```docker-compose down``` to remove the containers and network.


