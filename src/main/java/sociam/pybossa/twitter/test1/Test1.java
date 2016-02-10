package sociam.pybossa.twitter.test1;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Test1 {

	private static final String TWITTER_CONSUMER_KEY = "ZSouoRP3t2bLlznRn38LoABBY";
	private static final String TWITTER_SECRET_KEY = "x0sZsH9JR7oR5OjnEG2RO9Vbq74T4GuoYVd1TiUuhxxiddbZe9";
	private static final String TWITTER_ACCESS_TOKEN = "4895555638-q6ZVtqdcRIXgHCKgrN5qnSyQTy5xwL3ZcUrs1Rp";
	private static final String TWITTER_ACCESS_TOKEN_SECRET = "hxS9HSsIqUTyFEYoQxdSHQ8zPj31GMQ7zUwhlUwYQnO2K";

	public static void main(String[] args) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
				.setOAuthConsumerSecret(TWITTER_SECRET_KEY)
				.setOAuthAccessToken(TWITTER_ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(TWITTER_ACCESS_TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		try {
			Query query = new Query("MrEdPanama");
			QueryResult result;
			do {
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					System.out.println("@" + tweet.getUser().getScreenName()
							+ " - " + tweet.getText());
				}
			} while ((query = result.nextQuery()) != null);
			System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
			System.exit(-1);
		}
	}
}
