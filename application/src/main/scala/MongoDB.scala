package mongodb

import org.mongodb.scala._
import com.typesafe.config.ConfigFactory

object MongoDBConnection {

	val mongodbconfig = ConfigFactory.load().getConfig("mongodb")

	val connection_string:String = mongodbconfig.getString("CONNECTION_STRING")
	val database_name:String = mongodbconfig.getString("DATABASE")
	val collection_name:String = mongodbconfig.getString("COLLECTION")

	val mongoclient:MongoClient = MongoClient(connection_string)	
	val database:MongoDatabase = mongoclient.getDatabase(database_name)
	val collection:MongoCollection[Document] = database.getCollection(collection_name)

	val observer = new Observer[Completed] {
		override def onNext(result: Completed): Unit = Unit//println("Inserted")
		override def onError(e: Throwable): Unit = println("Failed")
		override def onComplete(): Unit = Unit//println("Completed")
	}

	def insert(JSONString:String) {
		val observable: Observable[Completed] = collection.insertOne(Document(JSONString))
		
		// Explictly subscribe:
		observable.subscribe(observer)
	}


}