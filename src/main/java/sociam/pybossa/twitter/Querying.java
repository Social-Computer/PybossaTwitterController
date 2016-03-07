package sociam.pybossa.twitter;

import sociam.pybossa.util.TwitterAccount;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class Querying {

	public static void main(String[] args) throws TwitterException {
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		Query query = new Query("from:RECOIN_val");
		QueryResult result = twitter.search(query);
		for (Status status : result.getTweets()) {
			System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
			twitter.destroyStatus(status.getId());
			System.out.println("Deleted");
		}
	}
}
