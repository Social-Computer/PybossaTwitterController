package sociam.pybossa.twitter.test1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TestTwitterJson {
	final static Logger logger = Logger.getLogger(TestTwitterJson.class);

	public static void main(String[] args) {

	}

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

}
