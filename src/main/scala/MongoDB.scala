package mongodb

import org.mongodb.scala._
import com.typesafe.config.ConfigFactory

object MongoDBConnection {

	val mongodbconfig = ConfigFactory.load().getConfig("mongodb")

	val mongoclient:MongoClient = MongoClient(mongodbconfig.getString("CONNECTION_STRING"))	

	val database:MongoDatabase = mongoclient.getDatabase(mongodbconfig.getString("DATABASE"))

	val collection:MongoCollection[Document] = database.getCollection(mongodbconfig.getString("COLLECTION"));

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