package sociam.pybossa.twitter;

import java.util.List;

import sociam.pybossa.util.TwitterAccount;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class DeleteTweets2 {

	public static void main(String[] args) throws InterruptedException, TwitterException {

		Boolean res = removeTweets();
		if (res == false) {
			removeTweets();
		} else if (res == true) {
			System.out.println("All tweets are deleted");
		} else {
			System.err.println("Error, exiting the script!!");
		}

	}

	public static Boolean removeTweets() {
		Twitter twitter = TwitterFactory.getSingleton();
		Twitter twitter2 = TwitterAccount.setTwitterAccount(2);
		try {
			Query query = new Query("source:twitter4j RECOIN_val");
			QueryResult result = twitter.search(query);

			while (result != null) {
				System.out.println("Number of status " + result.getTweets().size());
				for (Status status : result.getTweets()) {
					long id = status.getId();
					twitter2.destroyStatus(id);
					System.out.println("deleted");
					Thread.sleep(1000);
				}
				System.out.println("Waiting 15 minutes before getting 200 responses");
				result = twitter.search(query);
				Thread.sleep(900000);
			}

			return true;
		} catch (TwitterException e) {
			e.printStackTrace();
			if (e.exceededRateLimitation()) {
				try {
					System.err.println("Twitter rate limit is exceeded!");
					int waitfor = e.getRateLimitStatus().getSecondsUntilReset();
					System.err.println("Waiting for " + (waitfor + 100) + " seconds");
					Thread.sleep((waitfor * 1000) + 100000);
					removeTweets();
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
