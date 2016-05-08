package sociam.pybossa.test1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Status;
import twitter4j.Twitter;

public class TestProcessActivate {

	public static void main(String[] args) {

		// String text = "PODIUM JUMP!! Thank you Sochi! #RussianGP #F1 #TeamNR6
		// @MercedesAMGF1";
		// extractHashtags(text);
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		Status status = TwitterMethods.getTweetStausByID("727235036530020352", twitter);
		JSONObject json = statusToJson(status);
		System.out.println(json.toString());
		ObjectId id = MongodbMethods.inserNewtBin("saud", json);
		System.out.println(id);
		Long tweet_id = 234234L;
		if (id != null) {
			if (MongodbMethods.insertTaskIntoMongoDB(445, id.toString(), status.getText(), tweet_id, "ready", "validate")) {
			} else {
				System.err.println("Task was not inserted Into MongoDB");
			}
		}

	}

	public static String extractHashtags(String text) {

		String iden = "";
		try {
			Pattern p = Pattern.compile("[^a-zA-Z0-9]");
			StringTokenizer tokens = new StringTokenizer(text);
			ArrayList<String> hashtags = new ArrayList<String>();
			HashMap<String, Boolean> hashtags_map = new HashMap<String, Boolean>();
			String word = "";
			boolean toProcess = false;
			while (tokens.hasMoreTokens()) {
				word = tokens.nextToken();
				if (word.startsWith("#")) {
					word = word.replace("#", "").toLowerCase();
					boolean hasSpecialChar = p.matcher(word).find();
					if (!hasSpecialChar) {
						toProcess = true;
						hashtags_map.put(word, true);
					}
				}
			}
			if (toProcess) {
				for (Entry<String, Boolean> wrd : hashtags_map.entrySet()) {
					hashtags.add(wrd.getKey());
				}
				Collections.sort(hashtags, String.CASE_INSENSITIVE_ORDER);
				for (String ht : hashtags) {
					iden = iden + "_" + ht;
				}
				iden = iden.substring(1, iden.length());
				return iden;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

	}

	public static JSONObject statusToJson(Status status) {
		JSONObject tweetObj = new JSONObject();
		try {
			JSONArray mentions = new JSONArray();
			JSONObject geoLocation = new JSONObject();
			JSONArray hashtags = new JSONArray();
			JSONArray tweetUrls = new JSONArray();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			JSONObject retweetObj = new JSONObject();
			tweetObj.put("_source", "twitter_activate");
			tweetObj.put("id", status.getId());
			tweetObj.put("timestamp", dateFormat.format(status.getCreatedAt()));
			tweetObj.put("text", status.getText());
			tweetObj.put("screen_name", status.getUser().getScreenName());
			tweetObj.put("isRetweet", status.isRetweet());
			if (status.isRetweet()) {
				retweetObj.put("retweeted_tweet_id", status.getRetweetedStatus().getId());
				retweetObj.put("retweeted_tweet_timestamp", status.getRetweetedStatus().getCreatedAt().toString());
				retweetObj.put("retweeted_tweet_screen_name", status.getRetweetedStatus().getUser().getScreenName());
				tweetObj.put("retweet_tweet", retweetObj);
			}
			for (int i = 0; i < status.getUserMentionEntities().length; i++) {
				mentions.put(status.getUserMentionEntities()[i].getText());
			}
			tweetObj.put("mentions", mentions);

			for (int i = 0; i < status.getHashtagEntities().length; i++) {
				hashtags.put(status.getHashtagEntities()[i].getText());
			}
			tweetObj.put("hashtags", hashtags);
			try {
				geoLocation.put("lat", status.getGeoLocation().getLatitude());
				geoLocation.put("lng", status.getGeoLocation().getLongitude());
			} catch (Exception e) {
			}
			tweetObj.put("geo", geoLocation);
			for (int i = 0; i < status.getURLEntities().length; i++) {
				tweetUrls.put(status.getURLEntities()[i].getURL());
			}
			tweetObj.put("urls", tweetUrls);

			return tweetObj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
