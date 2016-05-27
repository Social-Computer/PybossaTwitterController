package recoin.mongodb_version;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.GeneralMethods;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Status;
import twitter4j.Twitter;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TwitterTaskCollector {

	final static Logger logger = Logger.getLogger(TwitterTaskCollector.class);
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat(
			"yyyy-mm-dd'T'hh:mm:ss.SSSSSS");
	final static String SOURCE = "Twitter";

	final static SimpleDateFormat Twitterformatter = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss ZZZZZ yyyy");
	// caching tasksIDs
	static HashMap<Integer, Integer> cachedTaskIDsAndProjectsIDs = new HashMap<>();

	static Twitter twitter;

	// in hours

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		twitter = TwitterAccount.setTwitterAccount(2);
		logger.info("TaskCollector will be repeated every "
				+ Config.TaskCollectorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskCollectorTrigger
						+ " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCollectorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}
	}

	public static void run() {
		try {

			logger.debug("Getting time line from Twitter");
			ArrayList<JSONObject> ResponsesFromTwitter = TwitterMethods
					.getMentionsTimelineAsJsons(twitter);
			if (ResponsesFromTwitter != null) {
				logger.debug("There are " + ResponsesFromTwitter.size()
						+ " tweets to be processed");
				for (JSONObject jsonObject : ResponsesFromTwitter) {

					String created_at = jsonObject.getString("created_at");
					Date created_at_date = Twitterformatter.parse(created_at);
					String id = jsonObject.getString("id_str");
					// two hours from now
					if (GeneralMethods
							.stopRetrivingTweetsAfterDate(
									created_at_date,
									Integer.valueOf(Config.twitterToDismissStatusesbeforeDate))) {
						logger.debug("Tweet with id "
								+ id
								+ " is older than "
								+ Integer
										.valueOf(Config.twitterToDismissStatusesbeforeDate)
								+ " hours!");
						continue;
					}
					// logger.debug("Processing a new twitter object ");
					if (!jsonObject.isNull("in_reply_to_status_id_str")) {

						String reply = jsonObject.getString("text");

						logger.debug("Found a reply tweet " + jsonObject);
						String in_reply_to_status_id_str = jsonObject
								.getString("in_reply_to_status_id_str");

						String in_reply_to_screen_name = jsonObject
								.getString("in_reply_to_screen_name");
						String taskResponse = reply.replaceAll("@"
								+ in_reply_to_screen_name, "");
						// // store the reply id
						// String id_str = jsonObject.getString("id_str");

						// store the use screen name
						JSONObject userJson = jsonObject.getJSONObject("user");

						// store the replier user name
						String screen_name = userJson.getString("screen_name");

						logger.debug("Checking if the reply has already being stored");
						// Document taskRun = getTaskRunsFromMongoDB(id_str);
						// if (taskRun == null) {

						if (reply.contains("ACTIVATE")) {
							logger.debug("Found an ACTIVATE json");

							Status status = TwitterMethods.getTweetStausByID(
									String.valueOf(in_reply_to_status_id_str),
									twitter);
							Boolean result = Activate.processACTIVATE(status,
									screen_name);
							if (result) {
								logger.debug("ACTIVATE task was successfuly stored with JSON "
										+ jsonObject.toString());
							} else {
								logger.error("Couldn't process the ACTIVATE task with "
										+ jsonObject.toString());
							}
							continue;
						}

						logger.debug("Looking for the original tweet for the reply");
						JSONObject orgTweet = TwitterMethods.getTweetByID(
								String.valueOf(in_reply_to_status_id_str),
								twitter);

						// loop through tweets till you find the orginal
						// tweet
						if (orgTweet != null) {
							while (!orgTweet
									.isNull("in_reply_to_status_id_str")) {
								orgTweet = TwitterMethods
										.getTweetByID(
												orgTweet.getString("in_reply_to_status_id_str"),
												twitter);
							}
							logger.debug("Original tweet was found");

							String orgTweetText = orgTweet.getString("text");
							Pattern pattern = Pattern.compile("(#t[0-9]+)");
							Matcher matcher = pattern.matcher(orgTweetText);
							String taskID = "";
							if (matcher.find()) {
								logger.debug("Found a taskID in the orginal tweet");
								taskID = matcher.group(1).replaceAll("#t", "");
								Integer intTaskID = Integer.valueOf(taskID);

								// cache taskIDs
								if (!cachedTaskIDsAndProjectsIDs
										.containsKey(intTaskID)) {
									logger.debug("TaskID is not in the cache");
									logger.debug("Retriving Task id from Collection: "
											+ Config.taskCollection);
									Document doc = MongodbMethods
											.getTaskFromMongoDB(intTaskID);
									if (doc != null) {
										int project_id = doc
												.getInteger("project_id");
										cachedTaskIDsAndProjectsIDs.put(
												intTaskID, project_id);
										if (MongodbMethods
												.insertTaskRun(taskResponse,
														intTaskID, project_id,
														screen_name, SOURCE)) {
											logger.debug("Task run was completely processed");

										} else {
											logger.error("Failed to process the task run");
										}
									} else {
										logger.error("Couldn't find task with ID "
												+ taskID);
										// TODO: Remove tweets that do not have
										// records in MongoDB
									}
								} else {
									logger.debug("Task ID was found in the cache");
									MongodbMethods.insertTaskRun(taskResponse,
											intTaskID,
											cachedTaskIDsAndProjectsIDs
													.get(intTaskID),
											screen_name, SOURCE);
								}
							} else {
								logger.error("reply: \\"
										+ reply
										+ " was not being identified with an associated task in the original text: \\"
										+ orgTweetText);
							}
						}
					}
				}
			} else {
				logger.info("Time line was null");
			}

			logger.debug("Adding task_run_id field to collection "
					+ Config.taskRunCollection);
			MongodbMethods.updateTaskRunsByAddingCounters();
		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

}
