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

	val cb = new ConfigurationBuilder
	cb.setDebugEnabled(true)
		.setOAuthConsumerKey(twittercredconfig.getString("CONSUMER_KEY"))
	  	.setOAuthConsumerSecret(twittercredconfig.getString("CONSUMER_KEY_SECRET"))
	  	.setOAuthAccessToken(twittercredconfig.getString("ACCESS_TOKEN"))
	  	.setOAuthAccessTokenSecret(twittercredconfig.getString("ACCESS_TOKEN_SECRET"))
	    .setJSONStoreEnabled(true)

	val statuslistener = new StatusListener {
    /*
    StatusListener defines what to do with the tweets as they stream
    */
		def onStatus(status:Status) {
	    	MessagePublisher.sendMessage(kafkaconfig.getString("TOPIC"), 
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
	val query = new FilterQuery().track(tweetfilterconfig.getString("KEYWORDS"))
								               .language(tweetfilterconfig.getString("LANGUAGES"))

  	def stream() {
		twitterStream.filter(query)
		// twitterStream.cleanUp()
		// twitterStream.shutdown()
	}

}