package sociam.pybossa.twitter;

import java.util.List;

import sociam.pybossa.util.TwitterAccount;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class DeleteTweets {

	public static void main(String[] args) throws InterruptedException {

		Boolean res = removeTweets();
		if (res == false) {
			removeTweets();
		}

	}

	public static Boolean removeTweets() {
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		try {
			Paging p = new Paging();
			p.setCount(200);
			List<Status> statuses = twitter.getHomeTimeline(p);
			while (statuses != null) {
				for (Status status : statuses) {
					long id = status.getId();
					twitter.destroyStatus(id);
					System.out.println("deleted");
					Thread.sleep(5000);
				}
				statuses = twitter.getHomeTimeline();
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
