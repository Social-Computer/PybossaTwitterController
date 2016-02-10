package sociam.pybossa.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class Main {

	final static Logger logger = Logger.getLogger(Main.class);
	HashSet<Long> ids = new HashSet<Long>();

	public void sendTask(String taskId, String taskContent) {
		// The factory instance is re-useable and thread safe.
		Twitter twitter = TwitterFactory.getSingleton();
		Status status;
		try {
			String post = taskContent + " #" + taskId;
			if (post.length() < 140) {
				status = twitter.updateStatus(post);
				if (ids.contains(status.getId())) {
					logger.info("Successfully posting a task ["
							+ status.getText() + "]." + status.getId());
				}
			} else {
				logger.error("Post \"" + post
						+ "\" is longer than 140 characters");
			}
		} catch (TwitterException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}

	}

	public ArrayList<String> getRepliesofTweet(long getInReplyToStatusId) {
		Twitter twitter = new TwitterFactory().getInstance();
		ArrayList<String> replies = new ArrayList<String>();
		try {
			User user = twitter.verifyCredentials();
			List<Status> statuses = twitter.getMentionsTimeline();
			System.out.println("Showing @" + user.getScreenName()
					+ "'s mentions.");
			for (Status status : statuses) {
				if (getInReplyToStatusId == status.getInReplyToStatusId()) {

					replies.add(status.getText());
					logger.info("@" + status.getUser().getScreenName() + " - "
							+ status.getText() + " id is: "
							+ status.getInReplyToStatusId());
				}
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
