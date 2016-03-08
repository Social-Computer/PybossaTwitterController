package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import sociam.pybossa.config.Config;
import sociam.pybossa.util.FacebookAccount;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class FacebookTaskCollector {

	final static Logger logger = Logger.getLogger(FacebookTaskCollector.class);
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat(
			"yyyy-mm-dd'T'hh:mm:ss.SSSSSS");
	final static String SOURCE = "Facebook";

	// caching tasksIDs
	static HashMap<Integer, Integer> cachedTaskIDsAndProjectsIDs = new HashMap<>();

	static Facebook facebook;

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		facebook = FacebookAccount.setFacebookAccount(2);
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

			logger.debug("Getting time line from facebook");
			ArrayList<JSONObject> ResponsesFromFacebook = getTimeLineAsJsons(facebook);
			if (ResponsesFromFacebook != null) {
				logger.debug("There are " + ResponsesFromFacebook.size()
						+ " tweets to be processed");
				for (JSONObject jsonObject : ResponsesFromFacebook) {

					// logger.debug("Processing a new facebook object ");
					if (!jsonObject.isNull("in_reply_to_status_id_str")) {
						logger.debug("Found a reply tweet " + jsonObject);
						String in_reply_to_status_id_str = jsonObject
								.getString("in_reply_to_status_id_str");
						String reply = jsonObject.getString("text");
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

						logger.debug("Looking for the original tweet for the reply");
						JSONObject orgTweet = getTweetByID(
								String.valueOf(in_reply_to_status_id_str),
								facebook);
						// loop through tweets till you find the orginal
						// tweet
						while (!orgTweet.isNull("in_reply_to_status_id_str")) {
							orgTweet = getTweetByID(
									orgTweet.getString("in_reply_to_status_id_str"),
									facebook);
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
								Document doc = getTaskFromMongoDB(intTaskID);
								if (doc != null) {
									int project_id = doc
											.getInteger("project_id");
									cachedTaskIDsAndProjectsIDs.put(intTaskID,
											project_id);
									if (insertTaskRun(taskResponse, intTaskID,
											project_id, screen_name, SOURCE)) {
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
								insertTaskRun(taskResponse, intTaskID,
										cachedTaskIDsAndProjectsIDs
												.get(intTaskID), screen_name,
										SOURCE);
							}

						} else {
							logger.error("reply: \\"
									+ reply
									+ " was not being identified with an associated task in the original text: \\"
									+ orgTweetText);
						}
					}

				}
			} else {
				logger.info("Time line was null");
			}

		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean insertTaskRun(String text, int task_id,
			int project_id, String contributor_name, String source) {

		Document taskRun = getTaskRunsFromMongoDB(task_id, contributor_name);
		if (taskRun != null) {
			logger.error("You are only allowed one contribution for each task.");
			logger.error("task_id= " + task_id + " screen_name: "
					+ contributor_name);
			return false;
		}

		JSONObject jsonData = BuildJsonTaskRunContent(text, task_id, project_id);
		if (insertTaskRunIntoMongoDB(jsonData, contributor_name, source)) {
			logger.debug("Task run was successfully inserted into MongoDB");
			// Project has to be reqested before inserting a task run
			logger.debug("Requesting the project ID from PyBossa before inserting it");
			String postURL = Config.PyBossahost + Config.taskRunDir
					+ Config.api_key;
			JSONObject postResponse = insertTaskRunIntoPyBossa(postURL,
					jsonData);
			if (postResponse != null) {
				logger.debug("Task run was successfully inserted into PyBossa");
				return true;
			} else {
				return false;
			}

		} else {
			logger.error("Task run was not inserted into MongoDB!");
			return false;
		}

	}

	public static Boolean insertTaskRunIntoMongoDB(JSONObject jsonData,
			String contributor_name, String source) {

		try {
			Date date = new Date();
			String insertedAt = MongoDBformatter.format(date);
			Integer project_id = jsonData.getInt("project_id");
			Integer task_id = jsonData.getInt("task_id");
			String task_run_text = jsonData.getString("info");
			logger.debug("Inserting a task run into MongoDB");
			if (pushTaskRunToMongoDB(insertedAt, project_id, task_id,
					task_run_text, contributor_name, source)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}

	}

	// maybe it's not needed to check id_str becasue we check it first!
	// so only do an insert?
	public static boolean pushTaskRunToMongoDB(String publishedAt,
			Integer project_id, Integer task_id, String task_text,
			String contributor_name, String source) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			if (publishedAt != null && project_id != null && task_text != null
					&& contributor_name != null && source != null) {

				database.getCollection(Config.taskRunCollection).insertOne(
						new Document().append("publishedAt", publishedAt)
								.append("project_id", project_id)
								.append("task_id", task_id)
								.append("task_text", task_text)
								.append("contributor_name", contributor_name)
								.append("source", source));
				logger.debug("One task run is inserted into MongoDB");
				mongoClient.close();
				return true;

			} else {
				mongoClient.close();
				return false;
			}

		} catch (Exception e) {
			logger.error("Error with inserting the task run " + " publishedAt "
					+ publishedAt + " project_id " + project_id + " isPushed "
					+ task_id + " task_id " + task_text + "\n", e);
			mongoClient.close();
			return false;
		}

	}

	public static JSONObject insertTaskRunIntoPyBossa(String url,
			JSONObject jsonData) {
		JSONObject jsonResult = null;
		logger.debug("Json to be inserted into PyBossa:  " + jsonData);
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
			logger.debug("Output from Server ...."
					+ response.getStatusLine().getStatusCode() + "\n");
			StringBuffer responseText = new StringBuffer();
			while ((output = br.readLine()) != null) {

				responseText.append(output);
			}
			jsonResult = new JSONObject(responseText);
			logger.debug("Post Response " + responseText);
			if (response.getStatusLine().getStatusCode() == 200
					|| response.getStatusLine().getStatusCode() == 204) {
				return jsonResult;
			} else {
				logger.error("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
				logger.error("Message " + response.getStatusLine());
				logger.error(response.toString());
				return null;
			}

		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}

	}

	/**
	 * It returns a json string for task creation from a given task details
	 * 
	 * @param text
	 *            the task content
	 * @param n_answers
	 * @param quorum
	 * @param calibration
	 * @param project_id
	 *            The project ID
	 * @param priority_0
	 * @return Json string
	 */
	public static JSONObject BuildJsonTaskRunContent(String answer,
			int task_id, int project_id) {

		JSONObject app2 = new JSONObject();
		app2.put("project_id", project_id);

		app2.put("info", answer);
		app2.put("task_id", task_id);
		Random rn = new Random();
		int randomNum = rn.nextInt((250 - 1) + 1) + 1;
		String ip = "80.44.192." + randomNum;
		app2.put("user_ip", ip);
		logger.debug("Generated ip " + ip);
		return app2;
	}

	public static Document getTaskFromMongoDB(int pybossa_task_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskCollection).find(
					new Document("pybossa_task_id", pybossa_task_id));
			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static Document getTaskRunsFromMongoDB(int task_id,
			String contributor_name) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskRunCollection).find(
					new Document("task_id", task_id).append("contributor_name",
							contributor_name));
			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static JSONObject getTweetByID(String status_id_str,
			Facebook facebook) {

		return new JSONObject();
	}

	public static ArrayList<JSONObject> getTimeLineAsJsons(Facebook facebook) {

		return new ArrayList<JSONObject>();

	}

}
