package sociam.pybossa.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.util.StringToImage;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TwitterMethods {

	final static Logger logger = Logger.getLogger(TwitterMethods.class);

	/**
	 * This method send a task by a twitter account
	 * 
	 * @param taskId
	 *            The id of the task to be hashed within the tweet
	 * @param taskContent
	 *            the content of the tweet to be published
	 */
	public static int sendTaskToTwitter(String taskContent, String media_url,
			String taskTag, ArrayList<String> hashtags, int project_type,
			String userTobeShared) {
		try {
			Twitter twitter = TwitterAccount.setTwitterAccount(project_type);

			// defualt
			String question = "";
			if (project_type == 2) {
				question = Config.project_validation_question;
			}

			// combine hashtags and tasktag while maintaining the 140 length
			String post = question;
			// for (String string : hashtags) {
			// if (post.length() == 0) {
			// post = string;
			// } else {
			// String tmpResult = post + " " + string + taskTag;
			// if (tmpResult.length() >= 140) {
			// break;
			// }
			// post = post + " " + string;
			// }
			// }
			// post = post + "?";
			String tag = taskTag.replaceAll("#t", "");
			post = post + " " + taskTag + " & monitor the task "
					+ Config.domainURI + tag;

			if (userTobeShared != null) {
				post = userTobeShared + " " + post;
			}
			// convert taskContent and question into an image
			File image = null;
			if (!media_url.equals("")) {
				image = StringToImage.combineTextWithImage(taskContent,
						media_url);
			} else {
				image = StringToImage.convertStringToImage(taskContent);
			}
			if (post.length() < 140) {
				// image must exist
				if (image != null) {
					// status = twitter.updateStatus(post);
					StatusUpdate status = new StatusUpdate(post);
					status.setMedia(image);
					twitter.updateStatus(status);
					logger.debug("Successfully posting a task '"
							+ status.getStatus() + "'." + status.getPlaceId());
					return 1;
				} else {
					logger.error("Image couldn't br generated");
					return 0;
				}
			} else {
				logger.error("Post \"" + post
						+ "\" is longer than 140 characters. It has: "
						+ (post.length()));
				return 0;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			return 2;
		}
	}

	public static int sendTaskToTwitterWithUrl(String taskTag,
			ArrayList<String> hashtags, int project_type,
			String userTobeShared, String tweet_url) {
		try {
			Twitter twitter = TwitterAccount.setTwitterAccount(project_type);
			String question = "";
			if (project_type == 2) {
				question = Config.project_validation_question;
			}
			// combine hashtags and tasktag while maintaining the 140 length
			String post = question;
			String tag = taskTag.replaceAll("#t", "");
			// post = post + " " + taskTag + " & monitor the task "
			// + Config.domainURI + tag;

			post = post
					+ "Reply or go to "
					+ Config.domainURI
					+ tag
					+ " to contribute instructions. HowTo: http://social-computer.org "
					+ taskTag;

			if (userTobeShared != null) {
				post = userTobeShared + " " + post;
			}

			if (post.length() < 160) {
				StatusUpdate status = new StatusUpdate(post + " " + tweet_url);
				twitter.updateStatus(status);
				logger.debug("Successfully posting a task '"
						+ status.getStatus() + "'." + status.getPlaceId());
				return 1;
			} else {
				logger.error("Post \"" + post
						+ "\" is longer than 140 characters. It has: "
						+ (post.length()));
				return 0;
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: ", te);
			if (te.exceededRateLimitation()) {
				try {
					logger.debug("Twitter rate limit is exceeded waiting for 300000 ms");
					Thread.sleep(300000);
					sendTaskToTwitterWithUrl(taskTag, hashtags, project_type,
							userTobeShared, tweet_url);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (te.getErrorCode() == 187) {
				return 3;
			}
			return 2;

		} catch (Exception e) {
			logger.error("Error", e);
			return 2;
		}
	}

	public static String redirectStatua(String url) {
		URLConnection con;
		try {
			con = new URL(url).openConnection();
			con.connect();
			InputStream is = con.getInputStream();
			is.close();
			return con.getURL().toString();
		} catch (IOException e) {
			logger.error("Error", e);
			return null;
		}
	}

	public static JSONObject getTweetByID(String status_id_str, Twitter twitter) {

		try {
			Status status = twitter.showStatus(Long.parseLong(status_id_str));
			if (status == null) { //
				return null;
			} else {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject json = new JSONObject(rawJSON);
				return json;
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: ", te);
			if (te.exceededRateLimitation()) {
				try {
					logger.debug("Twitter rate limit is exceeded waiting for 300000 ms");
					Thread.sleep(300000);
					getTweetByID(status_id_str, twitter);
				} catch (InterruptedException e) {
					logger.error("Error ", e);
					return null;
				}
			}
			return null;

		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}
	}

	public static Status getTweetStausByID(String status_id_str, Twitter twitter) {

		try {
			Status status = twitter.showStatus(Long.parseLong(status_id_str));
			return status;
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: ", te);
			if (te.exceededRateLimitation()) {
				try {
					logger.debug("Twitter rate limit is exceeded waiting for 300000 ms");
					Thread.sleep(300000);
					getTweetStausByID(status_id_str, twitter);
				} catch (InterruptedException e) {
					logger.error("Error ", e);
					e.printStackTrace();
					return null;
				}
			}
			return null;
		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}
	}

	public static ArrayList<JSONObject> getTimeLineAsJsons(Twitter twitter) {

		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		try {
			Paging p = new Paging();
			p.setCount(200);
			List<Status> statuses = twitter.getHomeTimeline(p);
			for (Status status : statuses) {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject jsonObject = new JSONObject(rawJSON);
				jsons.add(jsonObject);
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: ", te);
			if (te.exceededRateLimitation()) {
				try {
					logger.debug("Twitter rate limit is exceeded waiting for 300000 ms");
					Thread.sleep(300000);
					getTimeLineAsJsons(twitter);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
		return jsons;

	}

	public static ArrayList<JSONObject> getMentionsTimelineAsJsons(
			Twitter twitter) {
		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		try {
			Paging p = new Paging();
			p.setCount(200);
			List<Status> statuses = twitter.getMentionsTimeline(p);
			for (Status status : statuses) {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject jsonObject = new JSONObject(rawJSON);
				jsons.add(jsonObject);
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: ", te);
			if (te.exceededRateLimitation()) {
				try {
					logger.debug("Twitter rate limit is exceeded waiting for 300000 ms");
					Thread.sleep(300000);
					getMentionsTimelineAsJsons(twitter);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
		return jsons;
	}

	public static JSONObject getOembed(String url) {

		JSONObject jsons = new JSONObject();
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				jsons = new JSONObject(response.toString());
				in.close();
			}
			if (responseCode == 404) {
				JSONObject json = new JSONObject();
				json.put("error", "not there");
				return json;
			}
			return jsons;
		} catch (Exception te) {
			logger.error("Failed to get Oembed: ", te);
			return null;
		}
	}
}
