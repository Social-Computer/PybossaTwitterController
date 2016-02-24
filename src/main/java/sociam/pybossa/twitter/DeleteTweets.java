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
		Twitter twitter = TwitterAccount.setTwitterAccount(1);
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
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
