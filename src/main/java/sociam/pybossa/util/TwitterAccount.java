package sociam.pybossa.util;

import org.apache.log4j.Logger;

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
				cb.setDebugEnabled(true).setOAuthConsumerKey("ZSouoRP3t2bLlznRn38LoABBY")
						.setOAuthConsumerSecret("x0sZsH9JR7oR5OjnEG2RO9Vbq74T4GuoYVd1TiUuhxxiddbZe9")
						.setOAuthAccessToken("4895555638-q6ZVtqdcRIXgHCKgrN5qnSyQTy5xwL3ZcUrs1Rp")
						.setOAuthAccessTokenSecret("hxS9HSsIqUTyFEYoQxdSHQ8zPj31GMQ7zUwhlUwYQnO2K");

				// Transltion account
			} else if (i == 2) {
				cb.setDebugEnabled(true).setOAuthConsumerKey("2CKOAYT8OOAfS3mgSH5HOtXQ4")
						.setOAuthConsumerSecret("IIl53jtVCI8DHjcTww5t8bJcNqEuRyFxUJlFs1x9VH5bxlI3NK")
						.setOAuthAccessToken("4894867594-fY2u8giMiK4zMCJD8NtnuLRs6QFiEv2zBW7DZ08")
						.setOAuthAccessTokenSecret("GkW5FwC65EK7AjIgasnohb5QbFLojCiGWKwBoAANpB2eV");

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
