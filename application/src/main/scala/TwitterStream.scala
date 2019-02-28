package twitter

import kafka.MessagePublisher

import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization
import twitter4j.Status
import twitter4j._

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory


object TwitterStreamer {

  	val kafkaconfig = ConfigFactory.load().getConfig("kafka")
	val twittercredconfig = ConfigFactory.load().getConfig("twittercredentials")

	val CONSUMER_KEY:String = twittercredconfig.getString("CONSUMER_KEY")
	val CONSUMER_KEY_SECRET:String = twittercredconfig.getString("CONSUMER_KEY_SECRET")
	val ACCESS_TOKEN:String = twittercredconfig.getString("ACCESS_TOKEN")
	val ACCESS_TOKEN_SECRET:String = twittercredconfig.getString("ACCESS_TOKEN_SECRET")

	val kafkatopic:String = kafkaconfig.getString("TOPIC")

	val cb = new ConfigurationBuilder
	cb.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMER_KEY)
	  	.setOAuthConsumerSecret(CONSUMER_KEY_SECRET)
	  	.setOAuthAccessToken(ACCESS_TOKEN)
	  	.setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
	    .setJSONStoreEnabled(true)

	val statuslistener = new StatusListener {
    /*
    StatusListener defines what to do with the tweets as they stream
    */
		def onStatus(status:Status) {
	    	MessagePublisher.sendMessage(kafkatopic, 
                                   		 TwitterObjectFactory.getRawJSON(status))
		}
		def onDeletionNotice(statusDeletionNotice:StatusDeletionNotice) {
			println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId())
		}
		def onScrubGeo(userId:Long, upToStatusId:Long) {
			println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId)
		}
		def onStallWarning(warning:StallWarning) {
			println("Got stall warning:" + warning)
		}
		def onTrackLimitationNotice(numberOfLimitedStatuses:Int) {
			println("Got track limitation notice:" + numberOfLimitedStatuses)
		}
		def onException(ex:Exception) {
			ex.printStackTrace()
		}
	}

	val twitterStream = new TwitterStreamFactory(cb.build()).getInstance()
	twitterStream.addListener(statuslistener)

	val tweetfilterconfig = ConfigFactory.load().getConfig("tweetfilters")
	val keywords:String = tweetfilterconfig.getString("KEYWORDS")
	val languages:String = tweetfilterconfig.getString("LANGUAGES")

	val query = new FilterQuery().track(keywords)
								 .language(languages)

  	def stream() {
		twitterStream.filter(query)
		// twitterStream.cleanUp()
		// twitterStream.shutdown()
	}

}