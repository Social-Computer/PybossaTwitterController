package sociam.pybossa.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Main {

	final static Logger logger = Logger.getLogger(Main.class);
	HashSet<Long> ids = new HashSet<Long>();

	/**
	 * This method send a task by a twitter account
	 * 
	 * @param taskId
	 *            The id of the task to be hashed within the tweet
	 * @param taskContent
	 *            the content of the tweet to be published
	 */
	public Boolean sendTaskToTwitter(String taskId, String taskContent) {
		Twitter twitter = TwitterFactory.getSingleton();
		Status status;
		Boolean result;
		try {
			String post = taskContent + " #" + taskId;
			if (post.length() < 140) {
				status = twitter.updateStatus(post);
				logger.info("Successfully posting a task [" + status.getText() + "]." + status.getId());
				result = true;
			} else {
				logger.error("Post \"" + post + "\" is longer than 140 characters");
				result = false;
			}
		} catch (TwitterException e) {
			logger.error(e);
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		return result;

	}

	/**
	 * This method returns a list of strings contains all mentions by a user.
	 * 
	 * @param User
	 *            a twitter User object.
	 * @return a list of strings that contains the replies or null if empty
	 */
	public ArrayList<String> getListOfTweetsByUser(User user) {
		Twitter twitter = new TwitterFactory().getInstance();
		ArrayList<String> replies = new ArrayList<String>();
		try {
			user = twitter.verifyCredentials();
			List<Status> statuses = twitter.getMentionsTimeline();
			System.out.println("Showing @" + user.getScreenName() + "'s mentions.");
			for (Status status : statuses) {

				replies.add(status.getText());
				logger.info("@" + status.getUser().getScreenName() + " - " + status.getText() + " id is: "
						+ status.getInReplyToStatusId());
			}
			if (!replies.isEmpty()) {
				return replies;
			} else {
				return null;
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: " + te.getMessage());
			return null;
		}
	}

	/**
	 * This is a method that returns the timeline as a hashset of strings each
	 * contains a status encoded in Json
	 * 
	 * @param twitter
	 *            twitter object. Note: you should be getting this from method
	 *            "setTwitterAccount".
	 * @return A hashset of string each containing the a sepriate status as a
	 *         json.
	 */
	public static HashSet<String> getTimeLineAsJsons(Twitter twitter) {

		HashSet<String> jsons = new HashSet<String>();
		try {

			List<Status> statuses = twitter.getHomeTimeline();
			for (Status status : statuses) {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				logger.debug("Json retrived is: " + rawJSON);
				jsons.add(rawJSON);

			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: " + te.getMessage());
			return null;
		}

		return jsons;

	}

	// TODO: build the upper method for mapping between IDs and project type -
	// given that each project type should be related to a particular twitter
	// account.

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

			// Transltion account
			if (i == 1) {
				cb.setDebugEnabled(true).setOAuthConsumerKey("ZSouoRP3t2bLlznRn38LoABBY")
						.setOAuthConsumerSecret("x0sZsH9JR7oR5OjnEG2RO9Vbq74T4GuoYVd1TiUuhxxiddbZe9")
						.setOAuthAccessToken("4895555638-q6ZVtqdcRIXgHCKgrN5qnSyQTy5xwL3ZcUrs1Rp")
						.setOAuthAccessTokenSecret("hxS9HSsIqUTyFEYoQxdSHQ8zPj31GMQ7zUwhlUwYQnO2K");

				// Verfying account
			} else if (i == 2) {
				cb.setDebugEnabled(true).setOAuthConsumerKey("*********************")
						.setOAuthConsumerSecret("******************************************")
						.setOAuthAccessToken("**************************************************")
						.setOAuthAccessTokenSecret("******************************************");

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
			logger.error(e);
		} catch (TwitterException e) {
			logger.error(e);
		}
		return twitter;

	}
	/**
	 * This method returns a list of strings contains all mentions by a user.
	 * 
	 * @param User
	 *            a twitter User object.
	 * @return a list of strings that contains the replies or null if empty
	 */
	private static ArrayList<String> getListOfTweetsByUser(Twitter twitter) {
		twitter = new TwitterFactory().getInstance();
		ArrayList<String> replies = new ArrayList<String>();
		try {
			User user = twitter.verifyCredentials();
			List<Status> statuses = twitter.getMentionsTimeline();
			System.out.println("Showing @" + user.getScreenName()
					+ "'s mentions.");
			for (Status status : statuses) {

				replies.add(status.getText());
				logger.info("@" + status.getUser().getScreenName() + " - "
						+ status.getText() + " id is: "
						+ status.getInReplyToStatusId());
			}
			if (!replies.isEmpty()) {
				return replies;
			} else {
				return null;
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: " + te.getMessage());
			return null;
		}
	}
}
