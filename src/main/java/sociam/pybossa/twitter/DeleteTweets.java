package sociam.pybossa.twitter;

import java.util.List;

import sociam.pybossa.TwitterAccount;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class DeleteTweets {

	public static void main(String[] args) throws InterruptedException {

		Twitter twitter = TwitterAccount.setTwitterAccount(1);
		try {
			List<Status> statuses = twitter.getHomeTimeline();
			while (statuses != null) {
				for (Status status : statuses) {
					long id = status.getId();
					twitter.destroyStatus(id);
					System.out.println("deleted");
					Thread.sleep(5000);
				}
				statuses = twitter.getHomeTimeline();
			}

		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

}
