package recoin.mongodb_version;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

public class Activate {
	final static Logger logger = Logger.getLogger(Activate.class);

	public static boolean processACTIVATE(Status status, String screen_name) {
		String orgTweetText = status.getText();
		String projectName = extractHashtags(orgTweetText);
		Integer project_id = null;
		if (projectName != null) {
			logger.debug("Indentifed hashtag/s");
			JSONObject projectJson = MongodbMethods.getProjectByProject_name(projectName);
			if (projectJson == null) {
				logger.debug("Project has to be created");
				ArrayList<String> identifiers = getIdentifers(status);
				projectJson = createProjectObject(projectName, identifiers);
				logger.debug("Adding project_id field to collection "
						+ Config.projectCollection);
				MongodbMethods.updateProjectsByAddingCounters();
				project_id = MongodbMethods.insertProject(projectJson);
			} else {
				logger.debug("Project is already existed");
			}
			
			Long tweet_id = status.getId();
			project_id = projectJson.getInt("project_id");
			JSONObject json = statusToJson(status, screen_name);
			if (json != null) {
				logger.debug("Inserting task to bin");
				ObjectId id = MongodbMethods.inserNewtBin(projectName, json);
				if (id != null) {
					logger.debug("task to be inserted into " + Config.taskCollection);
					Boolean result = MongodbMethods.insertTaskIntoMongoDB(project_id, id.toString(), orgTweetText,
							tweet_id, "ready", "validate");
					if (result) {
						logger.debug("The insertion of activate process has been successful");
					} else {
						logger.error("Task was not inserted Into MongoDB");
					}
				}
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}

	}

	private static ArrayList<String> getIdentifers(Status status) {
		ArrayList<String> identifiers = new ArrayList<>();
		for (int i = 0; i < status.getHashtagEntities().length; i++) {
			identifiers.add(status.getHashtagEntities()[i].getText());
		}
		return identifiers;
	}

	public static JSONObject createProjectObject(String binName, ArrayList<String> identifiers) {

		JSONObject proj = new JSONObject();
		proj.put("project_id", "");
		proj.put("project_name", binName);
		proj.put("project_start_timestamp", "");
		proj.put("project_end_timestamp", "");
		proj.put("project_status", "ready");
		proj.put("observed", convertDateTimeToString(new Date()));
		proj.put("bin_id", binName);
		JSONArray binIDs = new JSONArray();
		for (String hashtag : identifiers) {
			binIDs.put(hashtag);
		}
		proj.put("identifiers", binIDs);
		return proj;
	}

	private static String convertDateTimeToString(Date date) {
		SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date_to_string = dateformatyyyyMMdd.format(date);
		return date_to_string;

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
			logger.error("Something went wrong!! ", e);
			return null;
		}

	}

	public static JSONObject statusToJson(Status status, String screen_name) {
		JSONObject tweetObj = new JSONObject();
		try {
			JSONArray mentions = new JSONArray();
			JSONObject geoLocation = new JSONObject();
			JSONArray hashtags = new JSONArray();
			JSONArray tweetUrls = new JSONArray();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			JSONObject retweetObj = new JSONObject();
			tweetObj.put("_source", "twitter_activatedBy_" + screen_name);
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
			try {
				String rawJson = TwitterObjectFactory.getRawJSON(status);
				tweetObj.put("status_raw", rawJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return tweetObj;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}
	}
}
