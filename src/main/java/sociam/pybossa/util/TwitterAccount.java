package sociam.pybossa.util;

import org.apache.log4j.Logger;

import sociam.pybossa.config.Config;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TwitterAccount {
	// TODO: build the upper method for mapping between IDs and project type -
	// given that each project type should be related to a particular twitter
	// account.
	final static Logger logger = Logger.getLogger(TwitterAccount.class);

	/**
	 * This is an intermediate method that is supposed to get a Twitter object
	 * based on a simple id mapping ( id=1 for trnaslation account).
	 * 
	 * @param i
	 *            This should be modelled somewhere else.
	 * @return Twitter object of a specific account.
	 */
	public static Twitter setTwitterAccount(int i) {
		Twitter twitter = null;
		try {
			logger.debug("Setting up a twitter account with its credintials!");
			ConfigurationBuilder cb = new ConfigurationBuilder();

			cb.setJSONStoreEnabled(true);

			// validation account
			if (i == 1) {
				cb.setDebugEnabled(true).setOAuthConsumerKey(Config.TwitterValidation1OAuthConsumerKey)
						.setOAuthConsumerSecret(Config.TwitterValidation1OAuthConsumerSecret)
						.setOAuthAccessToken(Config.TwitterValidation1OAuthAccessToken)
						.setOAuthAccessTokenSecret(Config.TwitterValidation1OAuthAccessTokenSecret);

				// Transltion account
			} else if (i == 2) {
				cb.setDebugEnabled(true).setOAuthConsumerKey(Config.TwitterValidation2OAuthConsumerKey)
						.setOAuthConsumerSecret(Config.TwitterValidation2OAuthConsumerSecret)
						.setOAuthAccessToken(Config.TwitterValidation2OAuthAccessToken)
						.setOAuthAccessTokenSecret(Config.TwitterValidation2OAuthAccessTokenSecret);

			} else if (i == 3) {
				cb.setDebugEnabled(true).setOAuthConsumerKey("*********************")
						.setOAuthConsumerSecret("******************************************")
						.setOAuthAccessToken("**************************************************")
						.setOAuthAccessTokenSecret("******************************************");

			} else if (i == 4) {
				cb.setDebugEnabled(true).setOAuthConsumerKey("*********************")
						.setOAuthConsumerSecret("******************************************")
						.setOAuthAccessToken("**************************************************")
						.setOAuthAccessTokenSecret("******************************************");

			} else {
				return null;
			}

			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();

			logger.debug("The twitter account " + twitter.getScreenName() + " is being set!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			logger.error("Error", e);
		} catch (TwitterException e) {
			logger.error("Errore", e);
		}
		return twitter;

	}
}
