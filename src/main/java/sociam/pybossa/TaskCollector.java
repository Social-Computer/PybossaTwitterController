package sociam.pybossa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TaskCollector {

	final static Logger logger = Logger.getLogger(TaskPerformer.class);
	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat(
			"yyyy-mm-dd'T'hh:mm:ss.SSSSSS");

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		logger.info("TaskCollector will be repeated every "
				+ Config.TaskCollectorTrigger + " ms");
		try {
			while (true) {
				run();
				logger.info("Sleeping for " + Config.TaskCollectorTrigger
						+ " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCollectorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error " + e);
		}

	}

	public static void run() {
		try {
			Twitter twitter = setTwitterAccount(1);
			ArrayList<JSONObject> ResponsesFromTwitter = getTimeLineAsJsons(twitter);
			if (ResponsesFromTwitter != null) {
				for (JSONObject jsonObject : ResponsesFromTwitter) {

					if (!jsonObject.isNull("in_reply_to_status_id_str")) {
						String in_reply_to_status_id_str = jsonObject
								.getString("in_reply_to_status_id_str");
						String reply = jsonObject.getString("text");
						String in_reply_to_screen_name = jsonObject
								.getString("in_reply_to_screen_name");
						String taskResponse = reply.replaceAll("@"
								+ in_reply_to_screen_name, "");

						JSONObject orgTweet = getTweetByID(
								String.valueOf(in_reply_to_status_id_str),
								twitter);
						// loop through tweets till you find the orginal tweet
						while (!orgTweet.isNull("in_reply_to_status_id_str")) {
							orgTweet = getTweetByID(
									orgTweet.getString("in_reply_to_status_id_str"),
									twitter);
						}

						String orgTweetText = orgTweet.getString("text");
						System.out.println("here " + orgTweetText);
						Pattern pattern = Pattern.compile("(#t[0-9]+)");
						Matcher matcher = pattern.matcher(orgTweetText);
						String taskID = "";
						if (matcher.find()) {
							taskID = matcher.group(1).replaceAll("#t", "");
							System.out.println(taskID);
							Document doc = getTaskFromMongoDB(Integer
									.valueOf(taskID));
							if (doc != null) {
								String project_id = doc.getString("project_id");
								insertTaskRun(taskResponse,
										Integer.valueOf(taskID),
										Integer.valueOf(project_id));

							} else {
								logger.error("Couldn't find task with ID "
										+ taskID);
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

		} catch (Exception e) {
			logger.error("Error " + e);
		}
	}

	private static Boolean insertTaskRun(String text, int task_id,
			int project_id) {

		JSONObject jsonData = BuildJsonTaskRunContent(text, task_id, project_id);
		if (getReqest(project_id)) {
			String url = Config.PyBossahost + Config.taskRunDir;
			JSONObject PyBossaResponse = insertTaskRunIntoPyBossa(url, jsonData);
			if (PyBossaResponse != null) {
				logger.debug("Task run was successfully inserted into PyBossa");
				if (insertTaskRunIntoMongoDB(PyBossaResponse)) {
					logger.debug("Task run was successfully inserted into MongoDB");
					return true;
				} else {
					logger.error("Task run was not inserted into MongoDB!");
					return false;
				}
			} else {
				logger.error("Task run was not inserted into PyBossa!");
				return false;
			}
		} else {
			return false;
		}

	}

	private static Boolean insertTaskRunIntoMongoDB(JSONObject PyBossaResponse) {

		try {
			Integer pybossa_task_run_id = PyBossaResponse.getInt("id");
			// String created_String = response.getString("created");
			// Date publishedAt = PyBossaformatter.parse(created_String);
			Date date = new Date();
			String insertedAt = MongoDBformatter.format(date);
			Integer project_id = PyBossaResponse.getInt("project_id");
			Integer task_id = PyBossaResponse.getInt("task_id");
			JSONObject info = PyBossaResponse.getJSONObject("info");
			String task_run_text = info.getString("text");
			logger.debug("Inserting a task into MongoDB");
			if (pushTaskRunToMongoDB(pybossa_task_run_id, insertedAt,
					project_id, task_id, task_run_text)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error " + e);
			return false;
		}

	}

	private static boolean pushTaskRunToMongoDB(Integer pybossa_task_run_id,
			String publishedAt, Integer project_id, Integer task_id,
			String task_text) {

		try {
			if (publishedAt != null && project_id != null && task_text != null) {

				FindIterable<Document> iterable = database.getCollection(
						Config.taskCollection)
						.find(new Document("pybossa_task_run_id",
								pybossa_task_run_id));
				if (iterable.first() == null) {
					database.getCollection(Config.taskRunCollection).insertOne(
							new Document()
									.append("pybossa_task_run_id",
											pybossa_task_run_id)
									.append("publishedAt", publishedAt)
									.append("project_id", project_id)
									.append("task_id", task_id)
									.append("task_text", task_text));
					logger.debug("One task is inserted into MongoDB");
					return true;
				} else {
					logger.error("task run is already in the collection!!");
					return false;
				}

			} else {
				return false;
			}

		} catch (Exception e) {
			logger.error("Error with inserting the task run "
					+ "pybossa_task_run_id " + pybossa_task_run_id
					+ "publishedAt " + publishedAt + "project_id " + project_id
					+ "isPushed " + task_id + "task_id " + task_text + "\n" + e);
			return false;
		}

	}

	private static JSONObject insertTaskRunIntoPyBossa(String url,
			JSONObject jsonData) {
		JSONObject jsonResult = null;
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString());
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200
					|| response.getStatusLine().getStatusCode() == 204) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(response.getEntity().getContent())));
				String output;
				logger.debug("Output from Server ...."
						+ response.getStatusLine().getStatusCode() + "\n");
				while ((output = br.readLine()) != null) {
					logger.debug(output);
					jsonResult = new JSONObject(output);
				}
				return jsonResult;
			} else {
				logger.error("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
				return null;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}

	}

	public static Boolean getReqest(int project_id) {
		String url = Config.PyBossahost + Config.projectDir + "/" + project_id
				+ "/newtask";

		HttpURLConnection con;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			// System.out.println("\nSending 'GET' request to URL : " + url);
			// System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			logger.debug(response);
			in.close();

			if (responseCode == 200 || responseCode == 204) {
				return true;
			} else {
				return false;
			}

		} catch (IOException e) {
			logger.error("Error " + e);
			return false;

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
	private static JSONObject BuildJsonTaskRunContent(String answer,
			int task_id, int project_id) {

		JSONObject app2 = new JSONObject();
		app2.put("project_id", 25);

		app2.put("info", answer);
		app2.put("task_id", 119);
		app2.put("user_ip", "80.44.145.144");
		return app2;
	}

	private static Document getTaskFromMongoDB(int pybossa_task_id) {
		try {
			MongoCollection<Document> collection = database
					.getCollection(Config.taskCollection);
			Document myDoc = collection.find(
					eq("pybossa_task_id", pybossa_task_id)).first();
			return myDoc;
		} catch (Exception e) {
			logger.error("Error " + e);
			return null;
		}

	}

	private static JSONObject getTweetByID(String status_id_str, Twitter twitter) {

		try {
			Status status = twitter.showStatus(Long.parseLong(status_id_str));
			if (status == null) { //
				return null;
			} else {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject json = new JSONObject(rawJSON);
				return json;
			}
		} catch (TwitterException e) {
			logger.error("Error " + e);
			return null;
		}
	}

	/**
	 * This method returns a list of strings contains all mentions by a user.
	 * 
	 * @param User
	 *            a twitter User object.
	 * @return a list of strings that contains the replies or null if empty
	 */
	private static ArrayList<String> getListOfTweetsByUser(Twitter twitter) {
		twitter = new TwitterFactory().getInstance();
		ArrayList<String> replies = new ArrayList<String>();
		try {
			User user = twitter.verifyCredentials();
			List<Status> statuses = twitter.getMentionsTimeline();
			System.out.println("Showing @" + user.getScreenName()
					+ "'s mentions.");
			for (Status status : statuses) {

				replies.add(status.getText());
				logger.info("@" + status.getUser().getScreenName() + " - "
						+ status.getText() + " id is: "
						+ status.getInReplyToStatusId());
			}
			if (!replies.isEmpty()) {
				return replies;
			} else {
				return null;
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: " + te.getMessage());
			return null;
		}
	}

	private static ArrayList<JSONObject> getTimeLineAsJsons(Twitter twitter) {

		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		try {

			List<Status> statuses = twitter.getHomeTimeline();
			for (Status status : statuses) {

				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject jsonObject = new JSONObject(rawJSON);
				jsons.add(jsonObject);
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: " + te.getMessage());
			return null;
		}

		return jsons;

	}

	// TODO: build the upper method for mapping between IDs and project type -
	// given that each project type should be related to a particular twitter
	// account.

	/**
	 * This is an intermediate method that is supposed to get a Twitter object
	 * based on a simple id mapping ( id=1 for trnaslation account).
	 * 
	 * @param i
	 *            This should be modelled somewhere else.
	 * @return Twitter object of a specific account.
	 */
	private static Twitter setTwitterAccount(int i) {
		Twitter twitter = null;
		try {
			logger.debug("Setting up a twitter account with its credintials!");
			ConfigurationBuilder cb = new ConfigurationBuilder();

			// Transltion account
			if (i == 1) {
				cb.setDebugEnabled(true)
						.setOAuthConsumerKey("ZSouoRP3t2bLlznRn38LoABBY")
						.setOAuthConsumerSecret(
								"x0sZsH9JR7oR5OjnEG2RO9Vbq74T4GuoYVd1TiUuhxxiddbZe9")
						.setOAuthAccessToken(
								"4895555638-q6ZVtqdcRIXgHCKgrN5qnSyQTy5xwL3ZcUrs1Rp")
						.setOAuthAccessTokenSecret(
								"hxS9HSsIqUTyFEYoQxdSHQ8zPj31GMQ7zUwhlUwYQnO2K");

				// Verfying account
			} else if (i == 2) {
				cb.setDebugEnabled(true)
						.setOAuthConsumerKey("*********************")
						.setOAuthConsumerSecret(
								"******************************************")
						.setOAuthAccessToken(
								"**************************************************")
						.setOAuthAccessTokenSecret(
								"******************************************");

			} else if (i == 3) {
				cb.setDebugEnabled(true)
						.setOAuthConsumerKey("*********************")
						.setOAuthConsumerSecret(
								"******************************************")
						.setOAuthAccessToken(
								"**************************************************")
						.setOAuthAccessTokenSecret(
								"******************************************");

			} else if (i == 4) {
				cb.setDebugEnabled(true)
						.setOAuthConsumerKey("*********************")
						.setOAuthConsumerSecret(
								"******************************************")
						.setOAuthAccessToken(
								"**************************************************")
						.setOAuthAccessTokenSecret(
								"******************************************");

			} else {
				return null;
			}

			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();

			logger.debug("The twitter account " + twitter.getScreenName()
					+ " is being set!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (TwitterException e) {
			logger.error(e);
		}
		return twitter;

	}

}
