package sociam.pybossa.twitter.test1;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class Test2 {

	public static void main(String[] args) {

		// posttweet();

		long aq = 697494640208056321L;
		getrepliesofTweet(aq);
	}

	public static void posttweet() {

		// The factory instance is re-useable and thread safe.
		Twitter twitter = TwitterFactory.getSingleton();
		Status status;
		try {
			status = twitter
					.updateStatus("Test posting another8 tweet by the app");
			System.out.println("Successfully updated the status to ["
					+ status.getText() + "]. " + status.getId());
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getrepliesofTweet(long getInReplyToStatusId) {
		// gets Twitter instance with default credentials
		Twitter twitter = new TwitterFactory().getInstance();
		try {
			User user = twitter.verifyCredentials();
			List<Status> statuses = twitter.getMentionsTimeline();
			System.out.println("Showing @" + user.getScreenName()
					+ "'s mentions.");
			for (Status status : statuses) {
				if (getInReplyToStatusId == status.getInReplyToStatusId()) {
					System.out.println("@" + status.getUser().getScreenName()
							+ " - " + status.getText() + " id is: "
							+ status.getInReplyToStatusId());
				}
			}
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		}
	}

}
